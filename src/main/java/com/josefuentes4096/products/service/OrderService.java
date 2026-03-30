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
    public OrderResponseDTO crearPedido(OrderRequestDTO request) {
        log.info("Creando pedido para usuario: {}", request.getUsuarioId());

        List<OrderItem> entidades = new ArrayList<>();
        List<OrderItemResponseDTO> responseItems = new ArrayList<>();
        double total = 0;

        for (OrderItemRequestDTO dto : request.getItems()) {

            Product product = productRepository.findById(dto.getProductoId())
                    .orElseThrow(() -> {
                        log.warn("Producto no encontrado con id: {}", dto.getProductoId());
                        return new ProductNotFoundException(dto.getProductoId());
                    });

            if (product.getStock() < dto.getCantidad()) {
                log.warn("Stock insuficiente para '{}': stock={}, solicitado={}", product.getNombre(), product.getStock(), dto.getCantidad());
                throw new InsufficientStockException(product.getNombre());
            }

            product.setStock(product.getStock() - dto.getCantidad());
            log.debug("Stock actualizado para '{}': nuevo stock={}", product.getNombre(), product.getStock());

            double subtotal = product.getPrecio() * dto.getCantidad();

            OrderItem item = new OrderItem();
            item.setProduct(product);
            item.setCantidad(dto.getCantidad());
            item.setSubtotal(subtotal);

            entidades.add(item);

            responseItems.add(new OrderItemResponseDTO(
                    product.getId(),
                    product.getNombre(),
                    dto.getCantidad(),
                    subtotal
            ));

            total += subtotal;
        }

        Order order = new Order();
        order.setUsuarioId(request.getUsuarioId());
        order.setEstado(OrderStatus.PENDIENTE);
        order.setTotal(total);
        order.setItems(entidades);

        entidades.forEach(item -> item.setOrder(order));

        Order saved = orderRepository.save(order);
        log.info("Pedido creado con id: {}, total: {}", saved.getId(), saved.getTotal());

        return new OrderResponseDTO(
                saved.getId(),
                saved.getUsuarioId(),
                saved.getEstado(),
                saved.getTotal(),
                responseItems
        );
    }

    public List<OrderResponseDTO> obtenerHistorial(Integer usuarioId) {
        log.info("Consultando historial de pedidos para usuario: {}", usuarioId);
        return orderRepository.findByUsuarioId(usuarioId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    private OrderResponseDTO toDTO(Order order) {
        List<OrderItemResponseDTO> items = order.getItems().stream()
                .map(item -> new OrderItemResponseDTO(
                        item.getProduct().getId(),
                        item.getProduct().getNombre(),
                        item.getCantidad(),
                        item.getSubtotal()
                ))
                .toList();
        return new OrderResponseDTO(
                order.getId(),
                order.getUsuarioId(),
                order.getEstado(),
                order.getTotal(),
                items
        );
    }
}