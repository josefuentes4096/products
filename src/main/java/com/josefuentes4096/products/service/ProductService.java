package com.josefuentes4096.products.service;

import com.josefuentes4096.products.dto.ProductRequestDTO;
import com.josefuentes4096.products.dto.ProductResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {

    Page<ProductResponseDTO> findAll(Pageable pageable);

    ProductResponseDTO findById(Integer id);

    ProductResponseDTO save(ProductRequestDTO dto);

    ProductResponseDTO update(Integer id, ProductRequestDTO dto);

    void delete(Integer id);

    ProductResponseDTO findByName(String name);

    Page<ProductResponseDTO> filterByCategory(String category, Pageable pageable);

    Page<ProductResponseDTO> findLowStock(Integer min, Pageable pageable);

    /**
     * Valida stock suficiente, lo descuenta y retorna el DTO actualizado.
     * Lanza InsufficientStockException si el stock es insuficiente.
     */
    ProductResponseDTO decreaseStock(Integer productId, Integer quantity);
}
