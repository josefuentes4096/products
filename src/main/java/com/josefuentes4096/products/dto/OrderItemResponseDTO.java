package com.josefuentes4096.products.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class OrderItemResponseDTO {
    private Integer productId;
    private String name;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
}
