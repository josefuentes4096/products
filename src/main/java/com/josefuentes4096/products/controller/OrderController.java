package com.josefuentes4096.products.controller;

import com.josefuentes4096.products.dto.OrderRequestDTO;
import com.josefuentes4096.products.dto.OrderResponseDTO;
import com.josefuentes4096.products.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponseDTO create(@RequestBody @Valid OrderRequestDTO request) {
        return service.createOrder(request);
    }

    @GetMapping("/user/{userId}")
    public List<OrderResponseDTO> getHistory(@PathVariable Integer userId) {
        return service.getOrderHistory(userId);
    }
}
