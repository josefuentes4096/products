package com.josefuentes4096.products.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProductResponseDTO {
    private Integer id;
    private String nombre;
    private String descripcion;
    private Double precio;
    private String categoria;
    private String imagenUrl;
    private Integer stock;
}