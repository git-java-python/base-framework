package com.example.framework.business.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Order {

    private final String orderId;
    private final String userId;
    private final String sku;
    private final int quantity;
    private final BigDecimal amount;
    private volatile OrderStatus status;
    private final LocalDateTime createdAt;
    private volatile LocalDateTime updatedAt;

    public Order(String orderId, String userId, String sku, int quantity, BigDecimal amount) {
        this.orderId = orderId;
        this.userId = userId;
        this.sku = sku;
        this.quantity = quantity;
        this.amount = amount;
        this.status = OrderStatus.INIT;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    public synchronized void transitTo(OrderStatus target) {
        if (!isValidTransition(this.status, target)) {
            throw new IllegalStateException("Invalid order transition: " + status + " -> " + target);
        }
        this.status = target;
        this.updatedAt = LocalDateTime.now();
    }

    private boolean isValidTransition(OrderStatus from, OrderStatus to) {
        return switch (from) {
            case INIT -> to == OrderStatus.PENDING_PAY;
            case PENDING_PAY -> to == OrderStatus.PAID || to == OrderStatus.CANCELLED;
            case PAID -> to == OrderStatus.FULFILLING || to == OrderStatus.REFUNDING;
            case FULFILLING -> to == OrderStatus.DONE || to == OrderStatus.REFUNDING;
            case REFUNDING -> to == OrderStatus.REFUNDED;
            default -> false;
        };
    }

    public String getOrderId() {
        return orderId;
    }

    public String getUserId() {
        return userId;
    }

    public String getSku() {
        return sku;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
