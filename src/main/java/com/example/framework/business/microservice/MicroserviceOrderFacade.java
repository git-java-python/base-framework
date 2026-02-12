package com.example.framework.business.microservice;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;

@Service
public class MicroserviceOrderFacade {

    private final CircuitBreaker userServiceBreaker = new CircuitBreaker(2, Duration.ofSeconds(10));

    public AggregatedView queryOrderWithUserProfile(String orderId, boolean simulateUserServiceFailure) {
        Map<String, Object> order = Map.of("orderId", orderId, "status", "FULFILLING", "amount", 188);

        Map<String, Object> user = userServiceBreaker.execute(
                () -> {
                    if (simulateUserServiceFailure) {
                        throw new IllegalStateException("user service timeout");
                    }
                    return Map.of("userId", "u1", "level", "GOLD", "riskTag", "NORMAL");
                },
                () -> Map.of("userId", "UNKNOWN", "level", "DEFAULT", "riskTag", "DEGRADED")
        );

        return new AggregatedView(
                order,
                user,
                "聚合模式 + 熔断降级，避免下游故障扩散",
                "隐患：降级数据可能不准确，需在UI提示",
                "优化：接入服务网格超时治理、重试预算与舱壁隔离"
        );
    }

    public record AggregatedView(
            Map<String, Object> order,
            Map<String, Object> user,
            String designReason,
            String risk,
            String optimization
    ) {}
}
