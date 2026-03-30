package com.josefuentes4096.products.service;

import com.josefuentes4096.products.entity.*;
import com.josefuentes4096.products.enums.OrderStatus;
import com.josefuentes4096.products.exception.InsufficientStockException;
import com.josefuentes4096.products.repository.OrderRepository;
import com.josefuentes4096.products.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    @Transactional
    public Order crearPedido(Order order) {
        double total = 0;

        for (OrderItem item : order.getItems()) {
            Product product = productRepository.findById(item.getProduct().getId())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

            if (product.getStock() < item.getCantidad()) {
                throw new InsufficientStockException(product.getNombre());
            }

            product.setStock(product.getStock() - item.getCantidad());

            double subtotal = product.getPrecio() * item.getCantidad();
            item.setSubtotal(subtotal);

            total += subtotal;
        }

        order.setEstado(OrderStatus.PENDIENTE);
        order.setTotal(total);

        return orderRepository.save(order);
    }
}