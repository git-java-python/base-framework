package com.example.framework.business.marketing;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class PromotionEngine {

    private final List<PromotionRule> rules;

    public PromotionEngine() {
        this.rules = List.of(
                new FullReductionRule(new BigDecimal("100"), new BigDecimal("20")),
                new PercentageRule(new BigDecimal("0.90"))
        );
    }

    public PromotionResult bestOffer(BigDecimal originAmount) {
        List<PromotionResult> candidates = new ArrayList<>();
        for (PromotionRule rule : rules) {
            candidates.add(rule.calculate(originAmount));
        }
        return candidates.stream()
                .min(Comparator.comparing(PromotionResult::payable))
                .orElse(new PromotionResult(originAmount, originAmount, "NO_PROMOTION"));
    }

    public sealed interface PromotionRule permits FullReductionRule, PercentageRule {
        PromotionResult calculate(BigDecimal originAmount);
    }

    public record PromotionResult(BigDecimal originAmount, BigDecimal payable, String explain) {}

    public record FullReductionRule(BigDecimal threshold, BigDecimal discount) implements PromotionRule {
        @Override
        public PromotionResult calculate(BigDecimal originAmount) {
            if (originAmount.compareTo(threshold) < 0) {
                return new PromotionResult(originAmount, originAmount, "FULL_REDUCTION_NOT_MATCHED");
            }
            BigDecimal payable = originAmount.subtract(discount).max(BigDecimal.ZERO);
            return new PromotionResult(originAmount, payable, "FULL_REDUCTION:" + threshold + "-" + discount);
        }
    }

    public record PercentageRule(BigDecimal rate) implements PromotionRule {
        @Override
        public PromotionResult calculate(BigDecimal originAmount) {
            BigDecimal payable = originAmount.multiply(rate);
            return new PromotionResult(originAmount, payable, "PERCENTAGE:" + rate);
        }
    }
}
