package com.example.framework.business;

import com.example.framework.business.dto.CreateOrderRequest;
import com.example.framework.business.infra.IdempotencyService;
import com.example.framework.business.infra.InventoryService;
import com.example.framework.business.infra.OutboxService;
import com.example.framework.business.marketing.PromotionEngine;
import com.example.framework.business.notification.NotificationService;
import com.example.framework.business.order.Order;
import com.example.framework.business.order.OrderService;
import com.example.framework.business.order.OrderStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class PlaybookServicesTest {

    @Test
    void shouldCreateAndPayOrderWithIdempotency() {
        IdempotencyService idempotencyService = new IdempotencyService();
        InventoryService inventoryService = new InventoryService();
        OutboxService outboxService = new OutboxService();
        OrderService orderService = new OrderService(idempotencyService, inventoryService, outboxService);

        CreateOrderRequest request = new CreateOrderRequest("req-1", "u1", "SKU-1001", 2, new BigDecimal("188"));
        Order first = orderService.createOrder(request);
        Order second = orderService.createOrder(request);

        assertEquals(first.getOrderId(), second.getOrderId());
        assertEquals(98, inventoryService.getAvailable("SKU-1001"));

        orderService.payOrder(first.getOrderId(), "txn-1");
        orderService.payOrder(first.getOrderId(), "txn-1");

        assertEquals(OrderStatus.FULFILLING, orderService.getOrder(first.getOrderId()).getStatus());
        assertEquals(2, outboxService.allEvents().size());
    }

    @Test
    void shouldPickBestPromotionAndApplyNotificationPolicy() {
        PromotionEngine engine = new PromotionEngine();
        var result = engine.bestOffer(new BigDecimal("200"));
        assertNotNull(result);
        assertEquals(new BigDecimal("180.00"), result.payable().setScale(2));

        NotificationService notificationService = new NotificationService(new IdempotencyService());
        var sent = notificationService.sendWithPolicy("biz-1", "user-1", "hello");
        var duplicate = notificationService.sendWithPolicy("biz-1", "user-1", "hello");

        assertEquals("SENT", sent.status());
        assertEquals("SKIPPED", duplicate.status());
    }
}
