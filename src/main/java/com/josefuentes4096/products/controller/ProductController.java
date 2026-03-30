package com.josefuentes4096.products.controller;

import com.josefuentes4096.products.dto.ProductRequestDTO;
import com.josefuentes4096.products.dto.ProductResponseDTO;
import com.josefuentes4096.products.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService service;

    @GetMapping
    public List<ProductResponseDTO> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public ProductResponseDTO obtener(@PathVariable Integer id) {
        return service.obtenerPorId(id);
    }

    @PostMapping
    public ProductResponseDTO crear(@RequestBody @Valid ProductRequestDTO dto) {
        return service.guardar(dto);
    }

    @PutMapping("/{id}")
    public ProductResponseDTO actualizar(@PathVariable Integer id,
                                         @RequestBody @Valid ProductRequestDTO dto) {
        return service.actualizar(id, dto);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Integer id) {
        service.eliminar(id);
    }

    @GetMapping("/buscar")
    public ProductResponseDTO buscarPorNombre(@RequestParam String nombre) {
        return service.buscarPorNombre(nombre);
    }

    @GetMapping("/categoria/{categoria}")
    public List<ProductResponseDTO> filtrarPorCategoria(
            @PathVariable String categoria) {
        return service.filtrarPorCategoria(categoria);
    }

    @GetMapping("/stock-minimo")
    public List<ProductResponseDTO> stockMinimo(
            @RequestParam(defaultValue = "5") Integer min) {
        return service.stockMinimo(min);
    }
}