package com.josefuentes4096.products.controller;

import com.josefuentes4096.products.dto.ProductRequestDTO;
import com.josefuentes4096.products.dto.ProductResponseDTO;
import com.josefuentes4096.products.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.ResponseStatus;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService service;

    @GetMapping
    public Page<ProductResponseDTO> findAll(
            @PageableDefault(size = 10, sort = "name") Pageable pageable) {
        return service.findAll(pageable);
    }

    @GetMapping("/{id}")
    public ProductResponseDTO findById(@PathVariable Integer id) {
        return service.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponseDTO create(@RequestBody @Valid ProductRequestDTO dto) {
        return service.save(dto);
    }

    @PutMapping("/{id}")
    public ProductResponseDTO update(@PathVariable Integer id,
                                     @RequestBody @Valid ProductRequestDTO dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Integer id) {
        service.delete(id);
    }

    @GetMapping("/search")
    public ProductResponseDTO findByName(@RequestParam String name) {
        return service.findByName(name);
    }

    @GetMapping("/category/{category}")
    public Page<ProductResponseDTO> filterByCategory(
            @PathVariable String category,
            @PageableDefault(size = 10, sort = "name") Pageable pageable) {
        return service.filterByCategory(category, pageable);
    }

    @GetMapping("/low-stock")
    public Page<ProductResponseDTO> findLowStock(
            @RequestParam(required = false) Integer min,
            @PageableDefault(size = 10, sort = "stock") Pageable pageable) {
        return service.findLowStock(min, pageable);
    }
}
