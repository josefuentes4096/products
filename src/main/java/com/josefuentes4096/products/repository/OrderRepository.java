package com.josefuentes4096.products.repository;

import com.josefuentes4096.products.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Integer> {
    List<Order> findByUsuarioId(Integer usuarioId);
}