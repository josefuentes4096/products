package com.josefuentes4096.products.controller;

import com.josefuentes4096.products.dto.OrderRequestDTO;
import com.josefuentes4096.products.dto.OrderResponseDTO;
import com.josefuentes4096.products.entity.Order;
import com.josefuentes4096.products.repository.OrderRepository;
import com.josefuentes4096.products.service.OrderService;
import jakarta.validation.Valid;
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
    public OrderResponseDTO crear(@RequestBody @Valid OrderRequestDTO request) {
        return service.crearPedido(request);
    }

    @GetMapping("/usuario/{usuarioId}")
    public List<Order> historial(@PathVariable Integer usuarioId) {
        return repository.findByUsuarioId(usuarioId);
    }
}