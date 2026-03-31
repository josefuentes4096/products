package com.josefuentes4096.products.repository;

import com.josefuentes4096.products.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Integer> {
    Page<Order> findByUserId(Integer userId, Pageable pageable);
}
