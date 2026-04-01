package com.josefuentes4096.products.event;

import java.math.BigDecimal;

public record OrderCreatedEvent(Integer orderId, Integer userId, BigDecimal total) {}
