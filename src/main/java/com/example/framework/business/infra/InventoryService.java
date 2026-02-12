package com.example.framework.business.infra;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class InventoryService {

    private final Map<String, Integer> inventory = new ConcurrentHashMap<>();
    private final Map<String, Integer> reserved = new ConcurrentHashMap<>();

    public InventoryService() {
        inventory.put("SKU-1001", 100);
        inventory.put("SKU-1002", 50);
    }

    public synchronized void reserve(String sku, int quantity) {
        int remain = inventory.getOrDefault(sku, 0);
        if (remain < quantity) {
            throw new IllegalStateException("insufficient inventory for sku=" + sku);
        }
        inventory.put(sku, remain - quantity);
        reserved.merge(sku, quantity, Integer::sum);
    }

    public synchronized void release(String sku, int quantity) {
        inventory.merge(sku, quantity, Integer::sum);
        reserved.computeIfPresent(sku, (k, v) -> Math.max(0, v - quantity));
    }

    public int getAvailable(String sku) {
        return inventory.getOrDefault(sku, 0);
    }
}
