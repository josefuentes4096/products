package com.josefuentes4096.products.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class ProductRequestDTO {

    @NotBlank(message = "El nombre es obligatorio")
    private String name;

    private String description;

    @Positive(message = "El precio debe ser mayor a 0")
    private Double price;

    private String category;

    private String imageUrl;

    @Min(value = 0, message = "El stock no puede ser negativo")
    private Integer stock;
}
