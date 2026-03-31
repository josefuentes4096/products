package com.josefuentes4096.products.service.impl;

import com.josefuentes4096.products.dto.*;
import com.josefuentes4096.products.entity.Order;
import com.josefuentes4096.products.entity.OrderItem;
import com.josefuentes4096.products.enums.OrderStatus;
import com.josefuentes4096.products.mapper.OrderMapper;
import com.josefuentes4096.products.repository.OrderRepository;
import com.josefuentes4096.products.repository.ProductRepository;
import com.josefuentes4096.products.service.OrderService;
import com.josefuentes4096.products.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductService productService;
    // ProductRepository se usa únicamente para obtener la referencia JPA al construir OrderItem.
    // Toda la lógica de negocio sobre productos (validación y descuento de stock) se delega a ProductService.
    private final ProductRepository productRepository;
    private final OrderMapper mapper;

    @Override
    @Transactional
    public OrderResponseDTO createOrder(OrderRequestDTO request) {
        log.info("Creando pedido para usuario: {}", request.getUserId());

        List<OrderItem> entities = new ArrayList<>();
        List<OrderItemResponseDTO> responseItems = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (OrderItemRequestDTO dto : request.getItems()) {
            // Delega validación de stock + descuento a ProductService (SRP/DIP)
            ProductResponseDTO product = productService.decreaseStock(dto.getProductId(), dto.getQuantity());

            BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(dto.getQuantity()));

            OrderItem item = new OrderItem();
            item.setProduct(productRepository.getReferenceById(dto.getProductId()));
            item.setQuantity(dto.getQuantity());
            item.setSubtotal(subtotal);
            entities.add(item);

            responseItems.add(new OrderItemResponseDTO(
                    product.getId(),
                    product.getName(),
                    dto.getQuantity(),
                    subtotal
            ));

            total = total.add(subtotal);
        }

        Order order = new Order();
        order.setUserId(request.getUserId());
        order.setStatus(OrderStatus.PENDING);
        order.setTotal(total);
        order.setItems(entities);
        entities.forEach(item -> item.setOrder(order));

        Order saved = orderRepository.save(order);
        log.info("Pedido creado con id: {}, total: {}", saved.getId(), saved.getTotal());

        return mapper.toDTO(saved, responseItems);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDTO> getOrderHistory(Integer userId) {
        log.debug("Consultando historial de pedidos para usuario: {}", userId);
        return orderRepository.findByUserId(userId)
                .stream()
                .map(mapper::toDTO)
                .toList();
    }
}
