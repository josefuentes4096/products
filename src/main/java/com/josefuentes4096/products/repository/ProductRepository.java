package com.josefuentes4096.products.repository;

import com.josefuentes4096.products.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Integer> {

    Optional<Product> findByName(String name);

    Page<Product> findByCategoryIgnoreCase(String category, Pageable pageable);

    Page<Product> findByStockLessThanEqual(Integer stock, Pageable pageable);
}
