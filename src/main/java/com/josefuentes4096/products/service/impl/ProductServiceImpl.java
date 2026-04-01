package com.josefuentes4096.products.service.impl;

import com.josefuentes4096.products.dto.ProductRequestDTO;
import com.josefuentes4096.products.dto.ProductResponseDTO;
import com.josefuentes4096.products.entity.Product;
import com.josefuentes4096.products.exception.InsufficientStockException;
import com.josefuentes4096.products.exception.ProductNotFoundException;
import com.josefuentes4096.products.mapper.ProductMapper;
import com.josefuentes4096.products.repository.ProductRepository;
import com.josefuentes4096.products.service.ProductService;
import com.josefuentes4096.products.service.SettingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository repository;
    private final ProductMapper mapper;
    private final SettingService settingService;

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponseDTO> findAll(Pageable pageable) {
        log.debug("Listando productos - página {}, tamaño {}", pageable.getPageNumber(), pageable.getPageSize());
        return repository.findAll(pageable).map(mapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponseDTO findById(Integer id) {
        log.debug("Buscando producto con id: {}", id);
        return mapper.toDTO(repository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Producto no encontrado con id: {}", id);
                    return new ProductNotFoundException(id);
                }));
    }

    @Override
    @Transactional
    public ProductResponseDTO save(ProductRequestDTO dto) {
        log.info("Creando producto: {}", dto.getName());
        ProductResponseDTO result = mapper.toDTO(repository.save(mapper.toEntity(dto)));
        log.info("Producto creado con id: {}", result.getId());
        return result;
    }

    @Override
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

        return mapper.toDTO(repository.save(product));
    }

    @Override
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

    @Override
    @Transactional(readOnly = true)
    public ProductResponseDTO findByName(String name) {
        log.debug("Buscando producto con nombre: {}", name);
        return repository.findByName(name)
                .map(mapper::toDTO)
                .orElseThrow(() -> {
                    log.warn("Producto no encontrado con nombre: {}", name);
                    return new ProductNotFoundException(name);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponseDTO> filterByCategory(String category, Pageable pageable) {
        log.debug("Filtrando productos por categoría: {}", category);
        return repository.findByCategoryIgnoreCase(category, pageable).map(mapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponseDTO> findLowStock(Integer min, Pageable pageable) {
        int threshold = min != null ? min : settingService.getMinimumStock();
        log.debug("Buscando productos con stock <= {}", threshold);
        return repository.findByStockLessThanEqual(threshold, pageable).map(mapper::toDTO);
    }

    @Override
    @Transactional
    public ProductResponseDTO decreaseStock(Integer productId, Integer quantity) {
        log.info("Descontando {} unidades del producto id: {}", quantity, productId);
        Product product = repository.findByIdForUpdate(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        if (product.getStock() < quantity) {
            log.warn("Stock insuficiente para '{}': stock={}, solicitado={}",
                    product.getName(), product.getStock(), quantity);
            throw new InsufficientStockException(product.getName());
        }

        product.setStock(product.getStock() - quantity);
        return mapper.toDTO(repository.save(product));
    }
}
