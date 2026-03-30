package com.josefuentes4096.products.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class ProductRequestDTO {

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    private String descripcion;

    @Positive(message = "El precio debe ser mayor a 0")
    private Double precio;

    private String categoria;

    private String imagenUrl;

    @Min(value = 0, message = "El stock no puede ser negativo")
    private Integer stock;
}