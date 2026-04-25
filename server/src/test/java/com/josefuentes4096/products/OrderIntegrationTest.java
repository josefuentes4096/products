package com.josefuentes4096.products;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.josefuentes4096.products.dto.OrderItemRequestDTO;
import com.josefuentes4096.products.dto.OrderRequestDTO;
import com.josefuentes4096.products.entity.Product;
import com.josefuentes4096.products.entity.Setting;
import com.josefuentes4096.products.exception.InsufficientStockException;
import com.josefuentes4096.products.repository.OrderRepository;
import com.josefuentes4096.products.repository.ProductRepository;
import com.josefuentes4096.products.repository.SettingRepository;
import com.josefuentes4096.products.service.ProductService;
import com.josefuentes4096.products.service.SettingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.cache.CacheManager;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OrderIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ProductRepository productRepository;
    @Autowired OrderRepository orderRepository;
    @Autowired SettingRepository settingRepository;
    @Autowired ProductService productService;
    @Autowired SettingService settingService;
    @Autowired CacheManager cacheManager;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();   // cascade a OrderItem
        productRepository.deleteAll();
        settingRepository.deleteAll();
        settingRepository.save(new Setting(null, "minimum_stock", "5"));
        cacheManager.getCache(SettingService.MINIMUM_STOCK_CACHE).clear();
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private Product savedProduct(String name, double price, String category, int stock) {
        return productRepository.save(new Product(null, name,
                "Descripción de " + name, BigDecimal.valueOf(price),
                category, name.toLowerCase().replace(" ", "_") + ".jpg",
                stock, null, null));
    }

    private OrderItemRequestDTO item(int productId, int quantity) {
        OrderItemRequestDTO dto = new OrderItemRequestDTO();
        dto.setProductId(productId);
        dto.setQuantity(quantity);
        return dto;
    }

    private String toJson(int userId, List<OrderItemRequestDTO> items) throws Exception {
        OrderRequestDTO req = new OrderRequestDTO();
        req.setUserId(userId);
        req.setItems(items);
        return objectMapper.writeValueAsString(req);
    }

    // -------------------------------------------------------------------------
    // Flujo completo: crear pedido y consultar historial
    // -------------------------------------------------------------------------

    @Test
    void crearPedidoConVariosItems_guardaReduceStockYApareceEnHistorial() throws Exception {
        Product strat = savedProduct("Fender Stratocaster", 1500.00, "Guitarras", 8);
        Product boss  = savedProduct("Boss DS-1",           80.00,   "Pedales",   20);

        // 1 Strat ($1500) + 2 Boss DS-1 ($160) = $1660
        mockMvc.perform(post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(7, List.of(item(strat.getId(), 1), item(boss.getId(), 2)))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(7))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.total").value(1660.00))
                .andExpect(jsonPath("$.items.length()").value(2));

        // Stock descontado correctamente
        assertThat(productRepository.findById(strat.getId()).orElseThrow().getStock()).isEqualTo(7);
        assertThat(productRepository.findById(boss.getId()).orElseThrow().getStock()).isEqualTo(18);

        // Historial incluye el pedido con nombre e items (ejercita OrderMapper.toItemDTO)
        mockMvc.perform(get("/api/v1/orders/user/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].total").value(1660.00))
                .andExpect(jsonPath("$.content[0].items.length()").value(2))
                .andExpect(jsonPath("$.content[0].items[0].name").value("Fender Stratocaster"));
    }

    @Test
    void crearPedidoDeUnSoloItem_calculaTotalYStockCorrectamente() throws Exception {
        Product marshall = savedProduct("Marshall DSL40CR", 1200.00, "Amplificadores", 5);

        // 2 Marshall x $1200 = $2400
        mockMvc.perform(post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(3, List.of(item(marshall.getId(), 2)))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.total").value(2400.00))
                .andExpect(jsonPath("$.items[0].quantity").value(2))
                .andExpect(jsonPath("$.items[0].subtotal").value(2400.00));

        assertThat(productRepository.findById(marshall.getId()).orElseThrow().getStock()).isEqualTo(3);
    }

    // -------------------------------------------------------------------------
    // Stock insuficiente
    // -------------------------------------------------------------------------

    @Test
    void crearPedido_retorna400YNoModificaStockSiNoHayUnidades() throws Exception {
        Product vox = savedProduct("Vox AC30", 2200.00, "Amplificadores", 1);

        mockMvc.perform(post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(1, List.of(item(vox.getId(), 5)))))
                .andExpect(status().isBadRequest());

        // Stock intacto, pedido no guardado
        assertThat(productRepository.findById(vox.getId()).orElseThrow().getStock()).isEqualTo(1);
        assertThat(orderRepository.count()).isZero();
    }

    // -------------------------------------------------------------------------
    // Producto inexistente
    // -------------------------------------------------------------------------

    @Test
    void crearPedido_retorna404SiElProductoNoExiste() throws Exception {
        mockMvc.perform(post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(1, List.of(item(9999, 1)))))
                .andExpect(status().isNotFound());

        assertThat(orderRepository.count()).isZero();
    }

    // -------------------------------------------------------------------------
    // Low-stock usando umbral de la BD (ejercita SettingService + Setting entity)
    // -------------------------------------------------------------------------

    @Test
    void findLowStock_retornaProductosBajoElUmbralDeLaBD() throws Exception {
        savedProduct("Marshall DSL40CR", 1200.00, "Amplificadores", 3);  // stock bajo
        savedProduct("Vox AC30",         2200.00, "Amplificadores", 1);  // stock bajo
        savedProduct("Fender Strat",     1500.00, "Guitarras",      10); // stock OK

        // Sin parámetro → usa minimum_stock=5 de la tabla settings
        mockMvc.perform(get("/api/v1/products/low-stock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2));
    }

    @Test
    void findLowStock_respetaParametroExplicitoSobreLaBD() throws Exception {
        savedProduct("Marshall DSL40CR", 1200.00, "Amplificadores", 3);
        savedProduct("Vox AC30",         2200.00, "Amplificadores", 1);
        savedProduct("Fender Strat",     1500.00, "Guitarras",      10);

        // Con min=2 → solo el Vox (stock=1) cumple
        mockMvc.perform(get("/api/v1/products/low-stock").param("min", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Vox AC30"));
    }

    // -------------------------------------------------------------------------
    // Historial vacío
    // -------------------------------------------------------------------------

    // -------------------------------------------------------------------------
    // Rollback parcial: si un ítem falla, ningún stock debe haberse modificado
    // -------------------------------------------------------------------------

    @Test
    void crearPedido_rollbackTotalSiSegundoItemTieneStockInsuficiente() throws Exception {
        Product strat = savedProduct("Fender Stratocaster", 1500.00, "Guitarras", 5);
        Product vox   = savedProduct("Vox AC30",           2200.00, "Amplificadores", 1);

        // El Vox tiene stock=1 pero pedimos 3 → falla en el segundo ítem
        OrderItemRequestDTO item1 = new OrderItemRequestDTO();
        item1.setProductId(strat.getId());
        item1.setQuantity(2);

        OrderItemRequestDTO item2 = new OrderItemRequestDTO();
        item2.setProductId(vox.getId());
        item2.setQuantity(3);

        OrderRequestDTO request = new OrderRequestDTO();
        request.setUserId(1);
        request.setItems(List.of(item1, item2));

        mockMvc.perform(post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        // Ningún stock debe haberse modificado (rollback completo)
        assertThat(productRepository.findById(strat.getId()).orElseThrow().getStock()).isEqualTo(5);
        assertThat(productRepository.findById(vox.getId()).orElseThrow().getStock()).isEqualTo(1);
        assertThat(orderRepository.count()).isZero();
    }

    // -------------------------------------------------------------------------
    // Concurrencia: el bloqueo pesimista evita stock negativo
    // -------------------------------------------------------------------------

    @Test
    void decreaseStock_conAccesoConcurrenteNoProduceStockNegativo() throws InterruptedException {
        Product product = savedProduct("Gibson Les Paul", 2500.00, "Guitarras", 5);
        int productId = product.getId();

        int totalHilos = 10; // 10 intentos simultáneos, solo 5 deben tener éxito
        CountDownLatch inicio = new CountDownLatch(1);
        CountDownLatch fin    = new CountDownLatch(totalHilos);
        AtomicInteger exitos  = new AtomicInteger(0);
        AtomicInteger fallos  = new AtomicInteger(0);

        ExecutorService executor = Executors.newFixedThreadPool(totalHilos);
        for (int i = 0; i < totalHilos; i++) {
            executor.submit(() -> {
                try {
                    inicio.await();
                    productService.decreaseStock(productId, 1);
                    exitos.incrementAndGet();
                } catch (InsufficientStockException e) {
                    fallos.incrementAndGet();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    fin.countDown();
                }
            });
        }

        inicio.countDown(); // lanzar todos los hilos al mismo tiempo
        assertThat(fin.await(15, TimeUnit.SECONDS)).isTrue();
        executor.shutdown();

        assertThat(exitos.get()).isEqualTo(5);
        assertThat(fallos.get()).isEqualTo(5);
        assertThat(productRepository.findById(productId).orElseThrow().getStock()).isEqualTo(0);
    }

    // -------------------------------------------------------------------------
    // Historial vacío
    // -------------------------------------------------------------------------

    @Test
    void getHistory_retornaListaVaciaParaUsuarioSinPedidos() throws Exception {
        mockMvc.perform(get("/api/v1/orders/user/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0));
    }

    // -------------------------------------------------------------------------
    // Cache: el valor se sirve desde caché tras la primera consulta
    // -------------------------------------------------------------------------

    @Test
    void getMinimumStock_sirveValorDesdeCacheTrasPrimeraConsulta() {
        // Primera llamada → consulta la BD (minimum_stock=5)
        assertThat(settingService.getMinimumStock()).isEqualTo(5);

        // Modificamos el valor en BD sin limpiar el caché
        settingRepository.deleteAll();
        settingRepository.save(new Setting(null, "minimum_stock", "99"));

        // Segunda llamada → debe devolver el valor cacheado (5), no el nuevo (99)
        assertThat(settingService.getMinimumStock()).isEqualTo(5);
    }
}
