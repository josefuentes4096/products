package com.josefuentes4096.products.service;

import com.josefuentes4096.products.dto.*;
import com.josefuentes4096.products.entity.Order;
import com.josefuentes4096.products.entity.Product;
import com.josefuentes4096.products.enums.OrderStatus;
import com.josefuentes4096.products.exception.InsufficientStockException;
import com.josefuentes4096.products.exception.ProductNotFoundException;
import com.josefuentes4096.products.repository.OrderRepository;
import com.josefuentes4096.products.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private ProductRepository productRepository;

    @InjectMocks
    private OrderService service;

    private Product stratocaster;
    private Product bossDS1;
    private Product marshallDsl40;
    private Product voxAC30;

    @BeforeEach
    void setUp() {
        stratocaster = new Product(1, "Fender Stratocaster American Pro II",
                "Guitarra eléctrica con pastillas V-Mod II",
                1500.00, "Guitarras", "fender_strat.jpg", 5);

        bossDS1 = new Product(2, "Boss DS-1 Distortion",
                "Pedal de distorsión compacto clásico",
                80.00, "Pedales", "boss_ds1.jpg", 20);

        marshallDsl40 = new Product(3, "Marshall DSL40CR",
                "Amplificador valvular de 40W dos canales",
                1200.00, "Amplificadores", "marshall_dsl40.jpg", 2);

        voxAC30 = new Product(4, "Vox AC30",
                "Amplificador valvular de 30W, icono del britpop",
                2200.00, "Amplificadores", "vox_ac30.jpg", 1);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private OrderRequestDTO buildRequest(int usuarioId, int productoId, int cantidad) {
        OrderItemRequestDTO item = new OrderItemRequestDTO();
        item.setProductoId(productoId);
        item.setCantidad(cantidad);

        OrderRequestDTO request = new OrderRequestDTO();
        request.setUsuarioId(usuarioId);
        request.setItems(List.of(item));
        return request;
    }

    private Order savedOrder(int id, int usuarioId, double total) {
        return new Order(id, usuarioId, OrderStatus.PENDIENTE, total, List.of());
    }

    // -------------------------------------------------------------------------
    // Pedido exitoso - un solo item
    // -------------------------------------------------------------------------

    @Test
    void crearPedido_compraDosGuitarrasCalculaTotalCorrecto() {
        // 2 Stratocasters x $1500 = $3000
        when(productRepository.findById(1)).thenReturn(Optional.of(stratocaster));
        when(orderRepository.save(any())).thenReturn(savedOrder(10, 7, 3000.00));

        OrderResponseDTO resultado = service.crearPedido(buildRequest(7, 1, 2));

        assertThat(resultado.getTotal()).isEqualTo(3000.00);
        assertThat(resultado.getEstado()).isEqualTo(OrderStatus.PENDIENTE);
        assertThat(resultado.getUsuarioId()).isEqualTo(7);
    }

    @Test
    void crearPedido_compraTresPedalesCalculaTotalCorrecto() {
        // 3 Boss DS-1 x $80 = $240
        when(productRepository.findById(2)).thenReturn(Optional.of(bossDS1));
        when(orderRepository.save(any())).thenReturn(savedOrder(11, 3, 240.00));

        OrderResponseDTO resultado = service.crearPedido(buildRequest(3, 2, 3));

        assertThat(resultado.getTotal()).isEqualTo(240.00);
    }

    @Test
    void crearPedido_estadoInicialSiempreEsPendiente() {
        when(productRepository.findById(3)).thenReturn(Optional.of(marshallDsl40));
        when(orderRepository.save(any())).thenReturn(savedOrder(12, 5, 1200.00));

        OrderResponseDTO resultado = service.crearPedido(buildRequest(5, 3, 1));

        assertThat(resultado.getEstado()).isEqualTo(OrderStatus.PENDIENTE);
    }

    // -------------------------------------------------------------------------
    // Decremento de stock
    // -------------------------------------------------------------------------

    @Test
    void crearPedido_decrementaStockDeGuitarraAlComprar() {
        // stock inicial: 5, compra: 2, stock final esperado: 3
        when(productRepository.findById(1)).thenReturn(Optional.of(stratocaster));
        when(orderRepository.save(any())).thenReturn(savedOrder(13, 1, 3000.00));

        service.crearPedido(buildRequest(1, 1, 2));

        assertThat(stratocaster.getStock()).isEqualTo(3);
    }

    @Test
    void crearPedido_decrementaStockDeAmplificadorAlComprar() {
        // Marshall: stock 2, compra 1, stock final: 1
        when(productRepository.findById(3)).thenReturn(Optional.of(marshallDsl40));
        when(orderRepository.save(any())).thenReturn(savedOrder(14, 1, 1200.00));

        service.crearPedido(buildRequest(1, 3, 1));

        assertThat(marshallDsl40.getStock()).isEqualTo(1);
    }

    @Test
    void crearPedido_agotaStockCuandoSeCompraExactamenteLoCantidadDisponible() {
        // Vox AC30: solo hay 1 en stock, se compra 1
        when(productRepository.findById(4)).thenReturn(Optional.of(voxAC30));
        when(orderRepository.save(any())).thenReturn(savedOrder(15, 2, 2200.00));

        service.crearPedido(buildRequest(2, 4, 1));

        assertThat(voxAC30.getStock()).isEqualTo(0);
    }

    // -------------------------------------------------------------------------
    // Stock insuficiente
    // -------------------------------------------------------------------------

    @Test
    void crearPedido_lanzaExcepcionSiSeIntentanComprarMasGuitarrasQueElStock() {
        // Stratocaster: stock 5, intento comprar 6
        when(productRepository.findById(1)).thenReturn(Optional.of(stratocaster));

        assertThatThrownBy(() -> service.crearPedido(buildRequest(1, 1, 6)))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("Fender Stratocaster American Pro II");

        verify(orderRepository, never()).save(any());
    }

    @Test
    void crearPedido_lanzaExcepcionSiAmplificadorEstaAgotado() {
        // Vox AC30: stock 1, se intenta comprar 2
        when(productRepository.findById(4)).thenReturn(Optional.of(voxAC30));

        assertThatThrownBy(() -> service.crearPedido(buildRequest(1, 4, 2)))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("Vox AC30");

        verify(orderRepository, never()).save(any());
    }

    @Test
    void crearPedido_lanzaExcepcionSiStockEsCero() {
        marshallDsl40.setStock(0);
        when(productRepository.findById(3)).thenReturn(Optional.of(marshallDsl40));

        assertThatThrownBy(() -> service.crearPedido(buildRequest(1, 3, 1)))
                .isInstanceOf(InsufficientStockException.class);

        verify(orderRepository, never()).save(any());
    }

    // -------------------------------------------------------------------------
    // Producto no encontrado
    // -------------------------------------------------------------------------

    @Test
    void crearPedido_lanzaExcepcionSiGuitarraNoExisteEnCatalogo() {
        when(productRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.crearPedido(buildRequest(1, 99, 1)))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void crearPedido_noGuardaPedidoSiElProductoNoExiste() {
        when(productRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.crearPedido(buildRequest(1, 99, 1)))
                .isInstanceOf(ProductNotFoundException.class);

        verify(orderRepository, never()).save(any());
    }

    // -------------------------------------------------------------------------
    // Múltiples items
    // -------------------------------------------------------------------------

    @Test
    void crearPedido_pedidoConGuitarraYPedalSumaTotalCorrectamente() {
        // 1 Stratocaster ($1500) + 2 Boss DS-1 ($160) = $1660
        OrderItemRequestDTO itemGuitarra = new OrderItemRequestDTO();
        itemGuitarra.setProductoId(1);
        itemGuitarra.setCantidad(1);

        OrderItemRequestDTO itemPedal = new OrderItemRequestDTO();
        itemPedal.setProductoId(2);
        itemPedal.setCantidad(2);

        OrderRequestDTO request = new OrderRequestDTO();
        request.setUsuarioId(10);
        request.setItems(List.of(itemGuitarra, itemPedal));

        when(productRepository.findById(1)).thenReturn(Optional.of(stratocaster));
        when(productRepository.findById(2)).thenReturn(Optional.of(bossDS1));
        when(orderRepository.save(any())).thenReturn(savedOrder(20, 10, 1660.00));

        OrderResponseDTO resultado = service.crearPedido(request);

        assertThat(resultado.getTotal()).isEqualTo(1660.00);
        assertThat(resultado.getUsuarioId()).isEqualTo(10);
    }

    @Test
    void crearPedido_pedidoConDosAmplificadoresDecorementaStockDeAmbos() {
        // 1 Marshall + 1 Vox AC30
        OrderItemRequestDTO itemMarshall = new OrderItemRequestDTO();
        itemMarshall.setProductoId(3);
        itemMarshall.setCantidad(1);

        OrderItemRequestDTO itemVox = new OrderItemRequestDTO();
        itemVox.setProductoId(4);
        itemVox.setCantidad(1);

        OrderRequestDTO request = new OrderRequestDTO();
        request.setUsuarioId(5);
        request.setItems(List.of(itemMarshall, itemVox));

        when(productRepository.findById(3)).thenReturn(Optional.of(marshallDsl40));
        when(productRepository.findById(4)).thenReturn(Optional.of(voxAC30));
        when(orderRepository.save(any())).thenReturn(savedOrder(21, 5, 3400.00));

        service.crearPedido(request);

        assertThat(marshallDsl40.getStock()).isEqualTo(1); // 2 - 1
        assertThat(voxAC30.getStock()).isEqualTo(0);       // 1 - 1
    }

    @Test
    void crearPedido_pedidoConVariosItemsCalculaSubtotalPorItem() {
        // 1 Stratocaster ($1500) + 5 Boss DS-1 ($400) = $1900
        OrderItemRequestDTO itemGuitarra = new OrderItemRequestDTO();
        itemGuitarra.setProductoId(1);
        itemGuitarra.setCantidad(1);

        OrderItemRequestDTO itemPedal = new OrderItemRequestDTO();
        itemPedal.setProductoId(2);
        itemPedal.setCantidad(5);

        OrderRequestDTO request = new OrderRequestDTO();
        request.setUsuarioId(8);
        request.setItems(List.of(itemGuitarra, itemPedal));

        when(productRepository.findById(1)).thenReturn(Optional.of(stratocaster));
        when(productRepository.findById(2)).thenReturn(Optional.of(bossDS1));

        Order saved = new Order(22, 8, OrderStatus.PENDIENTE, 1900.00, List.of());
        when(orderRepository.save(any())).thenReturn(saved);

        OrderResponseDTO resultado = service.crearPedido(request);

        assertThat(resultado.getPedidoId()).isEqualTo(22);
        assertThat(resultado.getTotal()).isEqualTo(1900.00);
    }
}
