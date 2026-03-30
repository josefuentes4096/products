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
    public Page<ProductResponseDTO> findAll(
            @PageableDefault(size = 10, sort = "name") Pageable pageable) {
        return service.findAll(pageable);
    }

    @GetMapping("/{id}")
    public ProductResponseDTO findById(@PathVariable Integer id) {
        return service.findById(id);
    }

    @PostMapping
    public ProductResponseDTO create(@RequestBody @Valid ProductRequestDTO dto) {
        return service.save(dto);
    }

    @PutMapping("/{id}")
    public ProductResponseDTO update(@PathVariable Integer id,
                                     @RequestBody @Valid ProductRequestDTO dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
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
            @RequestParam(defaultValue = "5") Integer min,
            @PageableDefault(size = 10, sort = "stock") Pageable pageable) {
        return service.findLowStock(min, pageable);
    }
}
