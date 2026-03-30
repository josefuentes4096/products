package com.josefuentes4096.products.service;

import com.josefuentes4096.products.dto.*;
import com.josefuentes4096.products.entity.*;
import com.josefuentes4096.products.enums.OrderStatus;
import com.josefuentes4096.products.exception.InsufficientStockException;
import com.josefuentes4096.products.exception.ProductNotFoundException;
import com.josefuentes4096.products.repository.OrderRepository;
import com.josefuentes4096.products.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    @Transactional
    public OrderResponseDTO createOrder(OrderRequestDTO request) {
        log.info("Creando pedido para usuario: {}", request.getUserId());

        List<OrderItem> entities = new ArrayList<>();
        List<OrderItemResponseDTO> responseItems = new ArrayList<>();
        double total = 0;

        for (OrderItemRequestDTO dto : request.getItems()) {

            Product product = productRepository.findById(dto.getProductId())
                    .orElseThrow(() -> {
                        log.warn("Producto no encontrado con id: {}", dto.getProductId());
                        return new ProductNotFoundException(dto.getProductId());
                    });

            if (product.getStock() < dto.getQuantity()) {
                log.warn("Stock insuficiente para '{}': stock={}, solicitado={}",
                        product.getName(), product.getStock(), dto.getQuantity());
                throw new InsufficientStockException(product.getName());
            }

            product.setStock(product.getStock() - dto.getQuantity());
            log.debug("Stock actualizado para '{}': nuevo stock={}", product.getName(), product.getStock());

            double subtotal = product.getPrice() * dto.getQuantity();

            OrderItem item = new OrderItem();
            item.setProduct(product);
            item.setQuantity(dto.getQuantity());
            item.setSubtotal(subtotal);

            entities.add(item);

            responseItems.add(new OrderItemResponseDTO(
                    product.getId(),
                    product.getName(),
                    dto.getQuantity(),
                    subtotal
            ));

            total += subtotal;
        }

        Order order = new Order();
        order.setUserId(request.getUserId());
        order.setStatus(OrderStatus.PENDING);
        order.setTotal(total);
        order.setItems(entities);

        entities.forEach(item -> item.setOrder(order));

        Order saved = orderRepository.save(order);
        log.info("Pedido creado con id: {}, total: {}", saved.getId(), saved.getTotal());

        return new OrderResponseDTO(
                saved.getId(),
                saved.getUserId(),
                saved.getStatus(),
                saved.getTotal(),
                responseItems
        );
    }

    public List<OrderResponseDTO> getOrderHistory(Integer userId) {
        log.info("Consultando historial de pedidos para usuario: {}", userId);
        return orderRepository.findByUserId(userId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    private OrderResponseDTO toDTO(Order order) {
        List<OrderItemResponseDTO> items = order.getItems().stream()
                .map(item -> new OrderItemResponseDTO(
                        item.getProduct().getId(),
                        item.getProduct().getName(),
                        item.getQuantity(),
                        item.getSubtotal()
                ))
                .toList();
        return new OrderResponseDTO(
                order.getId(),
                order.getUserId(),
                order.getStatus(),
                order.getTotal(),
                items
        );
    }
}
