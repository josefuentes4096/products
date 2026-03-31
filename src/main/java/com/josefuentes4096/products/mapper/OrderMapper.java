package com.josefuentes4096.products.mapper;

import com.josefuentes4096.products.dto.OrderItemResponseDTO;
import com.josefuentes4096.products.dto.OrderResponseDTO;
import com.josefuentes4096.products.entity.Order;
import com.josefuentes4096.products.entity.OrderItem;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderMapper {

    public OrderResponseDTO toDTO(Order order) {
        List<OrderItemResponseDTO> items = order.getItems().stream()
                .map(this::toItemDTO)
                .toList();
        return new OrderResponseDTO(
                order.getId(),
                order.getUserId(),
                order.getStatus(),
                order.getTotal(),
                items
        );
    }

    public OrderItemResponseDTO toItemDTO(OrderItem item) {
        return new OrderItemResponseDTO(
                item.getProduct().getId(),
                item.getProduct().getName(),
                item.getQuantity(),
                item.getSubtotal()
        );
    }
}
