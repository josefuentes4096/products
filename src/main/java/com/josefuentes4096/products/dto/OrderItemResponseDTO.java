package com.josefuentes4096.products.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OrderItemResponseDTO {
    private Integer productoId;
    private String nombre;
    private Integer cantidad;
    private Double subtotal;
}