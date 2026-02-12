package com.example.framework.business.infra;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class OutboxService {

    private final List<OutboxEvent> events = new CopyOnWriteArrayList<>();

    public void append(String topic, String key, String payload) {
        events.add(new OutboxEvent(topic, key, payload, LocalDateTime.now()));
    }

    public List<OutboxEvent> allEvents() {
        return Collections.unmodifiableList(new ArrayList<>(events));
    }

    public record OutboxEvent(String topic, String key, String payload, LocalDateTime createdAt) {}
}
