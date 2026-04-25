package com.josefuentes4096.products.service.impl;

import com.josefuentes4096.products.dto.*;
import com.josefuentes4096.products.entity.Order;
import com.josefuentes4096.products.entity.OrderItem;
import com.josefuentes4096.products.enums.OrderStatus;
import com.josefuentes4096.products.event.OrderCreatedEvent;
import com.josefuentes4096.products.mapper.OrderMapper;
import com.josefuentes4096.products.repository.OrderRepository;
import com.josefuentes4096.products.service.OrderService;
import com.josefuentes4096.products.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductService productService;
    private final OrderMapper mapper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public OrderResponseDTO createOrder(OrderRequestDTO request) {
        log.info("Creating order for user: {}", request.getUserId());

        List<OrderItem> entities = new ArrayList<>();
        List<OrderItemResponseDTO> responseItems = new ArrayList<>();

        for (OrderItemRequestDTO dto : request.getItems()) {
            ProductResponseDTO product = productService.decreaseStock(dto.getProductId(), dto.getQuantity());
            BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(dto.getQuantity()));
            entities.add(buildOrderItem(dto, product, subtotal));
            responseItems.add(buildOrderItemResponse(dto, product, subtotal));
        }

        Order saved = orderRepository.save(buildOrder(request, entities));
        log.info("Order created: id={}, total={}", saved.getId(), saved.getTotal());
        eventPublisher.publishEvent(new OrderCreatedEvent(saved.getId(), saved.getUserId(), saved.getTotal()));

        return mapper.toDTO(saved, responseItems);
    }

    private OrderItem buildOrderItem(OrderItemRequestDTO dto, ProductResponseDTO product, BigDecimal subtotal) {
        OrderItem item = new OrderItem();
        item.setProduct(productService.getReference(dto.getProductId()));
        item.setQuantity(dto.getQuantity());
        item.setUnitPrice(product.getPrice());
        item.setSubtotal(subtotal);
        return item;
    }

    private OrderItemResponseDTO buildOrderItemResponse(OrderItemRequestDTO dto, ProductResponseDTO product, BigDecimal subtotal) {
        return new OrderItemResponseDTO(product.getId(), product.getName(), dto.getQuantity(), product.getPrice(), subtotal);
    }

    private Order buildOrder(OrderRequestDTO request, List<OrderItem> items) {
        BigDecimal total = items.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Order order = new Order();
        order.setUserId(request.getUserId());
        order.setStatus(OrderStatus.PENDING);
        order.setTotal(total);
        order.setItems(items);
        items.forEach(item -> item.setOrder(order));
        return order;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponseDTO> getOrderHistory(Integer userId, Pageable pageable) {
        log.debug("Consultando historial de pedidos para usuario: {}", userId);
        return orderRepository.findByUserId(userId, pageable).map(mapper::toDTO);
    }
}
