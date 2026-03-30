package com.josefuentes4096.products.service;

import com.josefuentes4096.products.dto.ProductRequestDTO;
import com.josefuentes4096.products.dto.ProductResponseDTO;
import com.josefuentes4096.products.entity.Product;
import com.josefuentes4096.products.exception.ProductNotFoundException;
import com.josefuentes4096.products.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository repository;

    public Page<ProductResponseDTO> findAll(Pageable pageable) {
        log.info("Listando productos - página {}, tamaño {}", pageable.getPageNumber(), pageable.getPageSize());
        return repository.findAll(pageable).map(this::toDTO);
    }

    public ProductResponseDTO findById(Integer id) {
        log.info("Buscando producto con id: {}", id);
        return toDTO(repository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Producto no encontrado con id: {}", id);
                    return new ProductNotFoundException(id);
                }));
    }

    @Transactional
    public ProductResponseDTO save(ProductRequestDTO dto) {
        log.info("Creando producto: {}", dto.getName());
        Product product = toEntity(dto);
        ProductResponseDTO result = toDTO(repository.save(product));
        log.info("Producto creado con id: {}", result.getId());
        return result;
    }

    @Transactional
    public ProductResponseDTO update(Integer id, ProductRequestDTO dto) {
        log.info("Actualizando producto con id: {}", id);
        Product product = repository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Producto no encontrado con id: {}", id);
                    return new ProductNotFoundException(id);
                });

        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setCategory(dto.getCategory());
        product.setImageUrl(dto.getImageUrl());
        product.setStock(dto.getStock());

        return toDTO(repository.save(product));
    }

    @Transactional
    public void delete(Integer id) {
        log.info("Eliminando producto con id: {}", id);
        Product product = repository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Producto no encontrado con id: {}", id);
                    return new ProductNotFoundException(id);
                });
        repository.delete(product);
        log.info("Producto con id: {} eliminado", id);
    }

    public ProductResponseDTO findByName(String name) {
        log.info("Buscando producto con nombre: {}", name);
        return repository.findByName(name)
                .map(this::toDTO)
                .orElseThrow(() -> {
                    log.warn("Producto no encontrado con nombre: {}", name);
                    return new ProductNotFoundException(name);
                });
    }

    public Page<ProductResponseDTO> filterByCategory(String category, Pageable pageable) {
        log.info("Filtrando productos por categoría: {}", category);
        return repository.findByCategoryIgnoreCase(category, pageable).map(this::toDTO);
    }

    public Page<ProductResponseDTO> findLowStock(Integer min, Pageable pageable) {
        log.info("Buscando productos con stock <= {}", min);
        return repository.findByStockLessThanEqual(min, pageable).map(this::toDTO);
    }

    private ProductResponseDTO toDTO(Product p) {
        return new ProductResponseDTO(
                p.getId(),
                p.getName(),
                p.getDescription(),
                p.getPrice(),
                p.getCategory(),
                p.getImageUrl(),
                p.getStock()
        );
    }

    private Product toEntity(ProductRequestDTO dto) {
        Product p = new Product();
        p.setName(dto.getName());
        p.setDescription(dto.getDescription());
        p.setPrice(dto.getPrice());
        p.setCategory(dto.getCategory());
        p.setImageUrl(dto.getImageUrl());
        p.setStock(dto.getStock());
        return p;
    }
}
