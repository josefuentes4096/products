package com.josefuentes4096.products.service;

import com.josefuentes4096.products.dto.OrderRequestDTO;
import com.josefuentes4096.products.dto.OrderResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {

    OrderResponseDTO createOrder(OrderRequestDTO request);

    Page<OrderResponseDTO> getOrderHistory(Integer userId, Pageable pageable);
}
