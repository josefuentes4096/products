package com.josefuentes4096.products.repository;

import com.josefuentes4096.products.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Integer> {
}