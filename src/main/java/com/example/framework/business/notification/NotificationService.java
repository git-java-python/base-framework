package com.example.framework.business.notification;

import com.example.framework.business.infra.IdempotencyService;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class NotificationService {

    private final IdempotencyService idempotencyService;
    private final Map<String, LocalDateTime> lastSendTime = new ConcurrentHashMap<>();

    public NotificationService(IdempotencyService idempotencyService) {
        this.idempotencyService = idempotencyService;
    }

    public DeliveryResult sendWithPolicy(String bizId, String receiver, String content) {
        String dedupeKey = "MSG:" + bizId + ":" + receiver;
        if (idempotencyService.isDuplicate(dedupeKey, Duration.ofMinutes(5))) {
            return new DeliveryResult("SKIPPED", "duplicate message blocked");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime last = lastSendTime.get(receiver);
        if (last != null && last.plusSeconds(30).isAfter(now)) {
            return new DeliveryResult("DELAYED", "receiver hit frequency limit");
        }

        // 实际项目中应通过 MQ + 通道 SPI 异步发送
        lastSendTime.put(receiver, now);
        return new DeliveryResult("SENT", "content=" + content);
    }

    public record DeliveryResult(String status, String reason) {}
}
