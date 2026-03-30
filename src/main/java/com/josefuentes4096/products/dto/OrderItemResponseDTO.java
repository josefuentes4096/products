package com.josefuentes4096.products.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OrderItemResponseDTO {
    private Integer productId;
    private String name;
    private Integer quantity;
    private Double subtotal;
}
