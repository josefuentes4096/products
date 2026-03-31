package com.josefuentes4096.products.service;

import com.josefuentes4096.products.dto.*;
import com.josefuentes4096.products.entity.Order;
import com.josefuentes4096.products.entity.Product;
import com.josefuentes4096.products.enums.OrderStatus;
import com.josefuentes4096.products.exception.InsufficientStockException;
import com.josefuentes4096.products.exception.ProductNotFoundException;
import com.josefuentes4096.products.mapper.OrderMapper;
import com.josefuentes4096.products.repository.OrderRepository;
import com.josefuentes4096.products.repository.ProductRepository;
import com.josefuentes4096.products.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private ProductService productService;
    @Mock private ProductRepository productRepository;
    @Mock private OrderMapper mapper;

    @InjectMocks
    private OrderServiceImpl service;

    // DTOs de respuesta que retorna productService.decreaseStock()
    private ProductResponseDTO stratDTO;
    private ProductResponseDTO bossDTO;
    private ProductResponseDTO marshallDTO;
    private ProductResponseDTO voxDTO;

    @BeforeEach
    void setUp() {
        stratDTO   = new ProductResponseDTO(1, "Fender Stratocaster American Pro II",
                "Guitarra eléctrica con pastillas V-Mod II", BigDecimal.valueOf(1500.00), "Guitarras", "fender_strat.jpg", 3);
        bossDTO    = new ProductResponseDTO(2, "Boss DS-1 Distortion",
                "Pedal de distorsión compacto clásico",      BigDecimal.valueOf(80.00),   "Pedales",   "boss_ds1.jpg",    18);
        marshallDTO = new ProductResponseDTO(3, "Marshall DSL40CR",
                "Amplificador valvular de 40W dos canales",  BigDecimal.valueOf(1200.00), "Amplificadores", "marshall_dsl40.jpg", 1);
        voxDTO     = new ProductResponseDTO(4, "Vox AC30",
                "Amplificador valvular de 30W, icono del britpop", BigDecimal.valueOf(2200.00), "Amplificadores", "vox_ac30.jpg", 0);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private OrderRequestDTO buildRequest(int userId, int productId, int quantity) {
        OrderItemRequestDTO item = new OrderItemRequestDTO();
        item.setProductId(productId);
        item.setQuantity(quantity);

        OrderRequestDTO request = new OrderRequestDTO();
        request.setUserId(userId);
        request.setItems(List.of(item));
        return request;
    }

    private Order savedOrder(int id, int userId, double total) {
        return new Order(id, userId, OrderStatus.PENDING, BigDecimal.valueOf(total), List.of(), null, null);
    }

    // -------------------------------------------------------------------------
    // Pedido exitoso - un solo item
    // -------------------------------------------------------------------------

    @Test
    void createOrder_compraDosGuitarrasCalculaTotalCorrecto() {
        // 2 Stratocasters x $1500 = $3000
        when(productService.decreaseStock(1, 2)).thenReturn(stratDTO);
        when(orderRepository.save(any())).thenReturn(savedOrder(10, 7, 3000.00));

        OrderResponseDTO resultado = service.createOrder(buildRequest(7, 1, 2));

        assertThat(resultado.getTotal()).isEqualByComparingTo(BigDecimal.valueOf(3000.00));
        assertThat(resultado.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(resultado.getUserId()).isEqualTo(7);
    }

    @Test
    void createOrder_compraTresPedalesCalculaTotalCorrecto() {
        // 3 Boss DS-1 x $80 = $240
        when(productService.decreaseStock(2, 3)).thenReturn(bossDTO);
        when(orderRepository.save(any())).thenReturn(savedOrder(11, 3, 240.00));

        OrderResponseDTO resultado = service.createOrder(buildRequest(3, 2, 3));

        assertThat(resultado.getTotal()).isEqualByComparingTo(BigDecimal.valueOf(240.00));
    }

    @Test
    void createOrder_estadoInicialSiempreEsPending() {
        when(productService.decreaseStock(3, 1)).thenReturn(marshallDTO);
        when(orderRepository.save(any())).thenReturn(savedOrder(12, 5, 1200.00));

        OrderResponseDTO resultado = service.createOrder(buildRequest(5, 3, 1));

        assertThat(resultado.getStatus()).isEqualTo(OrderStatus.PENDING);
    }

    // -------------------------------------------------------------------------
    // Decremento de stock — se verifica que se delega a ProductService
    // -------------------------------------------------------------------------

    @Test
    void createOrder_decrementaStockDeGuitarraAlComprar() {
        when(productService.decreaseStock(1, 2)).thenReturn(stratDTO);
        when(orderRepository.save(any())).thenReturn(savedOrder(13, 1, 3000.00));

        service.createOrder(buildRequest(1, 1, 2));

        verify(productService).decreaseStock(1, 2);
    }

    @Test
    void createOrder_decrementaStockDeAmplificadorAlComprar() {
        when(productService.decreaseStock(3, 1)).thenReturn(marshallDTO);
        when(orderRepository.save(any())).thenReturn(savedOrder(14, 1, 1200.00));

        service.createOrder(buildRequest(1, 3, 1));

        verify(productService).decreaseStock(3, 1);
    }

    @Test
    void createOrder_agotaStockCuandoSeCompraExactamenteLaCantidadDisponible() {
        when(productService.decreaseStock(4, 1)).thenReturn(voxDTO);
        when(orderRepository.save(any())).thenReturn(savedOrder(15, 2, 2200.00));

        service.createOrder(buildRequest(2, 4, 1));

        verify(productService).decreaseStock(4, 1);
    }

    // -------------------------------------------------------------------------
    // Stock insuficiente
    // -------------------------------------------------------------------------

    @Test
    void createOrder_lanzaExcepcionSiSeIntentanComprarMasGuitarrasQueElStock() {
        when(productService.decreaseStock(1, 6))
                .thenThrow(new InsufficientStockException("Fender Stratocaster American Pro II"));

        assertThatThrownBy(() -> service.createOrder(buildRequest(1, 1, 6)))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("Fender Stratocaster American Pro II");

        verify(orderRepository, never()).save(any());
    }

    @Test
    void createOrder_lanzaExcepcionSiAmplificadorEstaAgotado() {
        when(productService.decreaseStock(4, 2))
                .thenThrow(new InsufficientStockException("Vox AC30"));

        assertThatThrownBy(() -> service.createOrder(buildRequest(1, 4, 2)))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("Vox AC30");

        verify(orderRepository, never()).save(any());
    }

    @Test
    void createOrder_lanzaExcepcionSiStockEsCero() {
        when(productService.decreaseStock(3, 1))
                .thenThrow(new InsufficientStockException("Marshall DSL40CR"));

        assertThatThrownBy(() -> service.createOrder(buildRequest(1, 3, 1)))
                .isInstanceOf(InsufficientStockException.class);

        verify(orderRepository, never()).save(any());
    }

    // -------------------------------------------------------------------------
    // Producto no encontrado
    // -------------------------------------------------------------------------

    @Test
    void createOrder_lanzaExcepcionSiGuitarraNoExisteEnCatalogo() {
        when(productService.decreaseStock(99, 1))
                .thenThrow(new ProductNotFoundException(99));

        assertThatThrownBy(() -> service.createOrder(buildRequest(1, 99, 1)))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void createOrder_noGuardaPedidoSiElProductoNoExiste() {
        when(productService.decreaseStock(99, 1))
                .thenThrow(new ProductNotFoundException(99));

        assertThatThrownBy(() -> service.createOrder(buildRequest(1, 99, 1)))
                .isInstanceOf(ProductNotFoundException.class);

        verify(orderRepository, never()).save(any());
    }

    // -------------------------------------------------------------------------
    // Múltiples items
    // -------------------------------------------------------------------------

    @Test
    void createOrder_pedidoConGuitarraYPedalSumaTotalCorrectamente() {
        // 1 Stratocaster ($1500) + 2 Boss DS-1 ($160) = $1660
        OrderItemRequestDTO itemGuitarra = new OrderItemRequestDTO();
        itemGuitarra.setProductId(1);
        itemGuitarra.setQuantity(1);

        OrderItemRequestDTO itemPedal = new OrderItemRequestDTO();
        itemPedal.setProductId(2);
        itemPedal.setQuantity(2);

        OrderRequestDTO request = new OrderRequestDTO();
        request.setUserId(10);
        request.setItems(List.of(itemGuitarra, itemPedal));

        when(productService.decreaseStock(1, 1)).thenReturn(stratDTO);
        when(productService.decreaseStock(2, 2)).thenReturn(bossDTO);
        when(orderRepository.save(any())).thenReturn(savedOrder(20, 10, 1660.00));

        OrderResponseDTO resultado = service.createOrder(request);

        assertThat(resultado.getTotal()).isEqualByComparingTo(BigDecimal.valueOf(1660.00));
        assertThat(resultado.getUserId()).isEqualTo(10);
    }

    @Test
    void createOrder_pedidoConDosAmplificadoresDecrementaStockDeAmbos() {
        // 1 Marshall + 1 Vox AC30
        OrderItemRequestDTO itemMarshall = new OrderItemRequestDTO();
        itemMarshall.setProductId(3);
        itemMarshall.setQuantity(1);

        OrderItemRequestDTO itemVox = new OrderItemRequestDTO();
        itemVox.setProductId(4);
        itemVox.setQuantity(1);

        OrderRequestDTO request = new OrderRequestDTO();
        request.setUserId(5);
        request.setItems(List.of(itemMarshall, itemVox));

        when(productService.decreaseStock(3, 1)).thenReturn(marshallDTO);
        when(productService.decreaseStock(4, 1)).thenReturn(voxDTO);
        when(orderRepository.save(any())).thenReturn(savedOrder(21, 5, 3400.00));

        service.createOrder(request);

        verify(productService).decreaseStock(3, 1);
        verify(productService).decreaseStock(4, 1);
    }

    @Test
    void createOrder_pedidoConVariosItemsCalculaSubtotalPorItem() {
        // 1 Stratocaster ($1500) + 5 Boss DS-1 ($400) = $1900
        OrderItemRequestDTO itemGuitarra = new OrderItemRequestDTO();
        itemGuitarra.setProductId(1);
        itemGuitarra.setQuantity(1);

        OrderItemRequestDTO itemPedal = new OrderItemRequestDTO();
        itemPedal.setProductId(2);
        itemPedal.setQuantity(5);

        OrderRequestDTO request = new OrderRequestDTO();
        request.setUserId(8);
        request.setItems(List.of(itemGuitarra, itemPedal));

        when(productService.decreaseStock(1, 1)).thenReturn(stratDTO);
        when(productService.decreaseStock(2, 5)).thenReturn(bossDTO);
        when(orderRepository.save(any())).thenReturn(savedOrder(22, 8, 1900.00));

        OrderResponseDTO resultado = service.createOrder(request);

        assertThat(resultado.getOrderId()).isEqualTo(22);
        assertThat(resultado.getTotal()).isEqualByComparingTo(BigDecimal.valueOf(1900.00));
    }
}
