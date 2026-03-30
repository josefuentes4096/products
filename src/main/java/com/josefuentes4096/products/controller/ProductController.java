package com.josefuentes4096.products.controller;

import com.josefuentes4096.products.entity.Product;
import com.josefuentes4096.products.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService service;

    @GetMapping
    public List<Product> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public Product obtener(@PathVariable Integer id) {
        return service.obtenerPorId(id);
    }

    @PostMapping
    public Product crear(@RequestBody Product product) {
        return service.guardar(product);
    }

    @PutMapping("/{id}")
    public Product actualizar(@PathVariable Integer id,
                               @RequestBody Product product) {
        return service.actualizar(id, product);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Integer id) {
        service.eliminar(id);
    }
}