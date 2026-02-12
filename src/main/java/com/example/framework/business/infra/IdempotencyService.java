package com.example.framework.business.infra;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class IdempotencyService {

    private final Map<String, Instant> handled = new ConcurrentHashMap<>();

    public boolean isDuplicate(String businessKey, Duration ttl) {
        Instant now = Instant.now();
        Instant exist = handled.get(businessKey);
        if (exist != null && exist.plus(ttl).isAfter(now)) {
            return true;
        }
        handled.put(businessKey, now);
        return false;
    }
}
