package com.example.framework.business.order;

import com.example.framework.business.dto.CreateOrderRequest;
import com.example.framework.business.infra.IdempotencyService;
import com.example.framework.business.infra.InventoryService;
import com.example.framework.business.infra.OutboxService;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OrderService {

    private final Map<String, Order> orderStore = new ConcurrentHashMap<>();
    private final Map<String, String> idempotentOrderMapping = new ConcurrentHashMap<>();

    private final IdempotencyService idempotencyService;
    private final InventoryService inventoryService;
    private final OutboxService outboxService;

    public OrderService(IdempotencyService idempotencyService,
                        InventoryService inventoryService,
                        OutboxService outboxService) {
        this.idempotencyService = idempotencyService;
        this.inventoryService = inventoryService;
        this.outboxService = outboxService;
    }

    public synchronized Order createOrder(CreateOrderRequest request) {
        String idempotentKey = request.userId() + ":" + request.requestId();
        String existingOrderId = idempotentOrderMapping.get(idempotentKey);
        if (existingOrderId != null) {
            return orderStore.get(existingOrderId);
        }
        if (idempotencyService.isDuplicate("ORDER_CREATE:" + idempotentKey, Duration.ofMinutes(10))) {
            throw new IllegalStateException("duplicate request in-flight");
        }

        inventoryService.reserve(request.sku(), request.quantity());

        Order order = new Order(com.example.framework.common.IdGenerator.uuid(),
                request.userId(), request.sku(), request.quantity(), request.amount());
        order.transitTo(OrderStatus.PENDING_PAY);
        orderStore.put(order.getOrderId(), order);
        idempotentOrderMapping.put(idempotentKey, order.getOrderId());

        outboxService.append("order-created", order.getOrderId(), "order created and pending pay");
        return order;
    }

    public synchronized Order payOrder(String orderId, String channelTxnId) {
        Order order = mustGet(orderId);
        if (idempotencyService.isDuplicate("PAY_CALLBACK:" + channelTxnId, Duration.ofHours(12))) {
            return order;
        }
        order.transitTo(OrderStatus.PAID);
        order.transitTo(OrderStatus.FULFILLING);
        outboxService.append("order-paid", order.getOrderId(), "order paid via txn " + channelTxnId);
        return order;
    }

    public synchronized Order cancelOrder(String orderId) {
        Order order = mustGet(orderId);
        order.transitTo(OrderStatus.CANCELLED);
        inventoryService.release(order.getSku(), order.getQuantity());
        outboxService.append("order-cancelled", order.getOrderId(), "order cancelled and inventory released");
        return order;
    }

    public Order getOrder(String orderId) {
        return mustGet(orderId);
    }

    private Order mustGet(String orderId) {
        Order order = orderStore.get(orderId);
        if (order == null) {
            throw new IllegalArgumentException("order not found: " + orderId);
        }
        return order;
    }
}
