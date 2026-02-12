package com.example.framework.business.microservice;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

public class CircuitBreaker {

    private final int failureThreshold;
    private final Duration openDuration;
    private final AtomicInteger failures = new AtomicInteger(0);
    private volatile State state = State.CLOSED;
    private volatile Instant openedAt;

    public CircuitBreaker(int failureThreshold, Duration openDuration) {
        this.failureThreshold = failureThreshold;
        this.openDuration = openDuration;
    }

    public synchronized <T> T execute(CheckedSupplier<T> supplier, CheckedSupplier<T> fallback) {
        if (state == State.OPEN && openedAt.plus(openDuration).isAfter(Instant.now())) {
            return fallback.get();
        }
        if (state == State.OPEN) {
            state = State.HALF_OPEN;
        }
        try {
            T result = supplier.get();
            failures.set(0);
            state = State.CLOSED;
            return result;
        } catch (Exception ex) {
            int failCount = failures.incrementAndGet();
            if (failCount >= failureThreshold) {
                state = State.OPEN;
                openedAt = Instant.now();
            }
            return fallback.get();
        }
    }

    public State getState() {
        return state;
    }

    public enum State {
        CLOSED, OPEN, HALF_OPEN
    }

    @FunctionalInterface
    public interface CheckedSupplier<T> {
        T get() throws Exception;
    }
}
