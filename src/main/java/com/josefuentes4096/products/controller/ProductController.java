package com.josefuentes4096.products.controller;

import com.josefuentes4096.products.dto.ProductRequestDTO;
import com.josefuentes4096.products.dto.ProductResponseDTO;
import com.josefuentes4096.products.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService service;

    @GetMapping
    public Page<ProductResponseDTO> listar(
            @PageableDefault(size = 10, sort = "nombre") Pageable pageable) {
        return service.listar(pageable);
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
    public Page<ProductResponseDTO> filtrarPorCategoria(
            @PathVariable String categoria,
            @PageableDefault(size = 10, sort = "nombre") Pageable pageable) {
        return service.filtrarPorCategoria(categoria, pageable);
    }

    @GetMapping("/stock-minimo")
    public Page<ProductResponseDTO> stockMinimo(
            @RequestParam(defaultValue = "5") Integer min,
            @PageableDefault(size = 10, sort = "stock") Pageable pageable) {
        return service.stockMinimo(min, pageable);
    }
}