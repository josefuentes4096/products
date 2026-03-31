package com.josefuentes4096.products.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.josefuentes4096.products.dto.OrderItemRequestDTO;
import com.josefuentes4096.products.dto.OrderRequestDTO;
import com.josefuentes4096.products.dto.OrderResponseDTO;
import com.josefuentes4096.products.enums.OrderStatus;
import com.josefuentes4096.products.exception.InsufficientStockException;
import com.josefuentes4096.products.exception.ProductNotFoundException;
import com.josefuentes4096.products.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
@WithMockUser
class OrderControllerTest {

    @Autowired MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();
    @MockitoBean OrderService service;

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

    private OrderResponseDTO buildResponse(int orderId, int userId, double total) {
        return new OrderResponseDTO(orderId, userId, OrderStatus.PENDING,
                BigDecimal.valueOf(total), List.of());
    }

    // -------------------------------------------------------------------------
    // POST /api/orders
    // -------------------------------------------------------------------------

    @Test
    void POST_create_retorna201AlCrearPedidoDeGuitarra() throws Exception {
        when(service.createOrder(any())).thenReturn(buildResponse(10, 7, 3000.00));

        mockMvc.perform(post("/api/orders").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(buildRequest(7, 1, 2))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").value(10))
                .andExpect(jsonPath("$.userId").value(7))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.total").value(3000.00));
    }

    @Test
    void POST_create_retorna201AlCrearPedidoConVariosItems() throws Exception {
        when(service.createOrder(any())).thenReturn(buildResponse(11, 3, 1660.00));

        OrderItemRequestDTO item1 = new OrderItemRequestDTO();
        item1.setProductId(1);
        item1.setQuantity(1);

        OrderItemRequestDTO item2 = new OrderItemRequestDTO();
        item2.setProductId(2);
        item2.setQuantity(2);

        OrderRequestDTO request = new OrderRequestDTO();
        request.setUserId(3);
        request.setItems(List.of(item1, item2));

        mockMvc.perform(post("/api/orders").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.total").value(1660.00));
    }

    @Test
    void POST_create_retorna400SiStockEsInsuficiente() throws Exception {
        when(service.createOrder(any()))
                .thenThrow(new InsufficientStockException("Fender Stratocaster American Pro II"));

        mockMvc.perform(post("/api/orders").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(buildRequest(1, 1, 100))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void POST_create_retorna404SiElProductoNoExiste() throws Exception {
        when(service.createOrder(any())).thenThrow(new ProductNotFoundException(99));

        mockMvc.perform(post("/api/orders").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(buildRequest(1, 99, 1))))
                .andExpect(status().isNotFound());
    }

    @Test
    void POST_create_retorna400SiUserIdEsNull() throws Exception {
        OrderRequestDTO request = buildRequest(1, 1, 1);
        request.setUserId(null);

        mockMvc.perform(post("/api/orders").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void POST_create_retorna400SiItemsEsVacio() throws Exception {
        OrderRequestDTO request = new OrderRequestDTO();
        request.setUserId(1);
        request.setItems(List.of());

        mockMvc.perform(post("/api/orders").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void POST_create_retorna400SiCantidadDeItemEsCero() throws Exception {
        OrderItemRequestDTO item = new OrderItemRequestDTO();
        item.setProductId(1);
        item.setQuantity(0);

        OrderRequestDTO request = new OrderRequestDTO();
        request.setUserId(1);
        request.setItems(List.of(item));

        mockMvc.perform(post("/api/orders").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void POST_create_retorna400SiCantidadDeItemEsNegativa() throws Exception {
        OrderItemRequestDTO item = new OrderItemRequestDTO();
        item.setProductId(1);
        item.setQuantity(-5);

        OrderRequestDTO request = new OrderRequestDTO();
        request.setUserId(1);
        request.setItems(List.of(item));

        mockMvc.perform(post("/api/orders").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // -------------------------------------------------------------------------
    // GET /api/orders/user/{userId}
    // -------------------------------------------------------------------------

    @Test
    void GET_getHistory_retornaPedidosDeUnUsuario() throws Exception {
        when(service.getOrderHistory(eq(7), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(buildResponse(1, 7, 3000.00),
                        buildResponse(2, 7, 240.00))));

        mockMvc.perform(get("/api/orders/user/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].orderId").value(1))
                .andExpect(jsonPath("$.content[1].orderId").value(2));
    }

    @Test
    void GET_getHistory_retornaListaVaciaSiUsuarioSinPedidos() throws Exception {
        when(service.getOrderHistory(eq(99), any(Pageable.class))).thenReturn(Page.empty());

        mockMvc.perform(get("/api/orders/user/99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0));
    }
}
