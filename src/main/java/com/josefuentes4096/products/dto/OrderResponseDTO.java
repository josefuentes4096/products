package com.josefuentes4096.products.dto;

import com.josefuentes4096.products.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class OrderResponseDTO {
    private Integer orderId;
    private Integer userId;
    private OrderStatus status;
    private Double total;
    private List<OrderItemResponseDTO> items;
}
