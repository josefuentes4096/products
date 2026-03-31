package com.josefuentes4096.products;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.josefuentes4096.products.dto.OrderItemRequestDTO;
import com.josefuentes4096.products.dto.OrderRequestDTO;
import com.josefuentes4096.products.entity.Product;
import com.josefuentes4096.products.entity.Setting;
import com.josefuentes4096.products.repository.OrderRepository;
import com.josefuentes4096.products.repository.ProductRepository;
import com.josefuentes4096.products.repository.SettingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

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

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();   // cascade a OrderItem
        productRepository.deleteAll();
        settingRepository.deleteAll();
        settingRepository.save(new Setting(null, "minimum_stock", "5"));
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
        mockMvc.perform(post("/api/orders")
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
        mockMvc.perform(get("/api/orders/user/7"))
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
        mockMvc.perform(post("/api/orders")
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

        mockMvc.perform(post("/api/orders")
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
        mockMvc.perform(post("/api/orders")
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
        mockMvc.perform(get("/api/products/low-stock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2));
    }

    @Test
    void findLowStock_respetaParametroExplicitoSobreLaBD() throws Exception {
        savedProduct("Marshall DSL40CR", 1200.00, "Amplificadores", 3);
        savedProduct("Vox AC30",         2200.00, "Amplificadores", 1);
        savedProduct("Fender Strat",     1500.00, "Guitarras",      10);

        // Con min=2 → solo el Vox (stock=1) cumple
        mockMvc.perform(get("/api/products/low-stock").param("min", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Vox AC30"));
    }

    // -------------------------------------------------------------------------
    // Historial vacío
    // -------------------------------------------------------------------------

    @Test
    void getHistory_retornaListaVaciaParaUsuarioSinPedidos() throws Exception {
        mockMvc.perform(get("/api/orders/user/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0));
    }
}
