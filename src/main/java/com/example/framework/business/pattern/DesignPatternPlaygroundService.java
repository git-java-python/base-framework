package com.example.framework.business.pattern;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
public class DesignPatternPlaygroundService {

    public PatternResult run(String payType, BigDecimal amount, boolean vip) {
        PaymentStrategy strategy = PaymentStrategyFactory.create(payType);
        BigDecimal payable = strategy.pay(amount);

        List<OrderCheckHandler> chain = List.of(new InventoryCheck(), new RiskCheck(vip));
        for (OrderCheckHandler handler : chain) {
            handler.check();
        }

        return new PatternResult(
                Map.of("payType", payType, "origin", amount, "payable", payable),
                "策略模式+工厂模式+责任链",
                "策略解耦支付算法，工厂收敛创建逻辑，责任链可插拔校验",
                "隐患：策略与工厂扩展不规范会膨胀",
                "优化：结合SPI自动发现策略，责任链配置化"
        );
    }

    interface PaymentStrategy {
        BigDecimal pay(BigDecimal amount);
    }

    static class AliPayStrategy implements PaymentStrategy {
        public BigDecimal pay(BigDecimal amount) { return amount.multiply(new BigDecimal("0.98")); }
    }

    static class WxPayStrategy implements PaymentStrategy {
        public BigDecimal pay(BigDecimal amount) { return amount.multiply(new BigDecimal("0.99")); }
    }

    static class CouponPayStrategy implements PaymentStrategy {
        public BigDecimal pay(BigDecimal amount) { return amount.subtract(new BigDecimal("10")).max(BigDecimal.ZERO); }
    }

    static class PaymentStrategyFactory {
        static PaymentStrategy create(String payType) {
            return switch (payType.toUpperCase()) {
                case "ALIPAY" -> new AliPayStrategy();
                case "WXPAY" -> new WxPayStrategy();
                case "COUPON" -> new CouponPayStrategy();
                default -> amount -> amount;
            };
        }
    }

    interface OrderCheckHandler {
        void check();
    }

    static class InventoryCheck implements OrderCheckHandler {
        @Override
        public void check() {
            // demo: pass
        }
    }

    static class RiskCheck implements OrderCheckHandler {
        private final boolean vip;

        RiskCheck(boolean vip) {
            this.vip = vip;
        }

        @Override
        public void check() {
            if (!vip) {
                // demo: 非VIP仍允许下单，仅记录风险评分
            }
        }
    }

    public record PatternResult(
            Map<String, Object> payload,
            String usedPatterns,
            String designReason,
            String risk,
            String optimization
    ) {}
}
