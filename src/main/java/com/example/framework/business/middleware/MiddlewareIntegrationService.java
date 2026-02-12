package com.example.framework.business.middleware;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MiddlewareIntegrationService {

    private final Map<String, Object> localCache = new ConcurrentHashMap<>();

    public MiddlewareResult queryProduct(String sku) {
        Object cached = localCache.get(sku);
        if (cached != null) {
            return new MiddlewareResult("CACHE_HIT", cached,
                    "缓存前置降低DB压力",
                    "隐患：缓存一致性与穿透问题",
                    "优化：布隆过滤器+互斥锁重建+双删策略");
        }

        Map<String, Object> dbData = Map.of(
                "sku", sku,
                "price", 199,
                "updatedAt", LocalDateTime.now().toString()
        );
        localCache.put(sku, dbData);
        return new MiddlewareResult("DB_HIT", dbData,
                "先查缓存后查库，回填缓存",
                "隐患：热点key失效瞬间可能击穿",
                "优化：热点永不过期+异步刷新");
    }

    public MiddlewareResult publishOrderEvent(String orderId) {
        Map<String, Object> event = Map.of("topic", "order-event", "orderId", orderId, "status", "PUBLISHED");
        return new MiddlewareResult("MQ_PUBLISH", event,
                "通过MQ解耦交易与下游系统",
                "隐患：消息重复/积压",
                "优化：幂等消费者+延迟队列+死信处理");
    }

    public MiddlewareResult lockAndExecute(String resource) {
        return new MiddlewareResult("LOCK_EXECUTED", Map.of("resource", resource),
                "分布式锁保护临界资源",
                "隐患：锁续期失败与误释放",
                "优化：Redisson看门狗+业务超时兜底");
    }

    public record MiddlewareResult(
            String type,
            Object payload,
            String designReason,
            String risk,
            String optimization
    ) {}
}
