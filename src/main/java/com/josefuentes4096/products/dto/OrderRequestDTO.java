package com.josefuentes4096.products.dto;

import lombok.Data;

import java.util.List;

@Data
public class OrderRequestDTO {
    private Integer usuarioId;
    private List<OrderItemRequestDTO> items;
}