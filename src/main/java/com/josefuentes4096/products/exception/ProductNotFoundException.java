package com.josefuentes4096.products.exception;

public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(Integer id) {
        super("Producto no encontrado con id: " + id);
    }
}