package com.josefuentes4096.products.service;

import com.josefuentes4096.products.dto.OrderRequestDTO;
import com.josefuentes4096.products.dto.OrderResponseDTO;

import java.util.List;

public interface OrderService {

    OrderResponseDTO createOrder(OrderRequestDTO request);

    List<OrderResponseDTO> getOrderHistory(Integer userId);
}
