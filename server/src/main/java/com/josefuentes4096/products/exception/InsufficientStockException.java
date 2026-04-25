package com.josefuentes4096.products.exception;

public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(String nombre) {
        super("Stock insuficiente para el producto: " + nombre);
    }
}