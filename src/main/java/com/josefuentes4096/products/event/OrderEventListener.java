package com.josefuentes4096.products.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderEventListener {

    @EventListener
    @Async
    public void onOrderCreated(OrderCreatedEvent event) {
        log.info("Order processed: id={}, userId={}, total={}",
                event.orderId(), event.userId(), event.total());
        // Future: send confirmation email, trigger analytics, alert on low stock, etc.
    }
}
