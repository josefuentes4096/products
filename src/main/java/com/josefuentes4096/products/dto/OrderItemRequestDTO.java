package com.josefuentes4096.products.dto;

import lombok.Data;

@Data
public class OrderItemRequestDTO {
    private Integer productoId;
    private Integer cantidad;
}