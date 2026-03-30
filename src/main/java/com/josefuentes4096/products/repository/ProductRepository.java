package com.josefuentes4096.products.repository;

import com.josefuentes4096.products.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Integer> {

    Optional<Product> findByNombre(String nombre);

    List<Product> findByCategoriaIgnoreCase(String categoria);

    List<Product> findByStockLessThanEqual(Integer stock);
}
