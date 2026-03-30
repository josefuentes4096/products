package com.josefuentes4096.products.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class OrderItemRequestDTO {
    @NotNull(message = "El id del producto es obligatorio")
    private Integer productId;

    @Positive(message = "La cantidad debe ser mayor a 0")
    private Integer quantity;
}
