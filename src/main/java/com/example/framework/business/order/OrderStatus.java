package com.example.framework.business.order;

public enum OrderStatus {
    INIT,
    PENDING_PAY,
    PAID,
    FULFILLING,
    DONE,
    CANCELLED,
    REFUNDING,
    REFUNDED
}
