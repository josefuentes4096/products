package com.josefuentes4096.products.dto;

import com.josefuentes4096.products.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class OrderResponseDTO {
    private Integer pedidoId;
    private Integer usuarioId;
    private OrderStatus estado;
    private Double total;
    private List<OrderItemResponseDTO> items;
}