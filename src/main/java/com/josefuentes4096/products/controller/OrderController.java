package com.josefuentes4096.products.controller;

import com.josefuentes4096.products.entity.Order;
import com.josefuentes4096.products.repository.OrderRepository;
import com.josefuentes4096.products.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService service;
    private final OrderRepository repository;

    @PostMapping
    public Order crear(@RequestBody Order order) {
        return service.crearPedido(order);
    }

    @GetMapping("/usuario/{usuarioId}")
    public List<Order> historial(@PathVariable Integer usuarioId) {
        return repository.findByUsuarioId(usuarioId);
    }
}