package com.example.framework.business.tx;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DistributedTransactionService {

    public TxResult executeSaga(boolean paymentFail) {
        List<String> steps = new ArrayList<>();
        List<String> compensations = new ArrayList<>();

        steps.add("create-order");
        steps.add("reserve-inventory");

        if (paymentFail) {
            steps.add("pay-failed");
            compensations.add("compensate-inventory");
            compensations.add("cancel-order");
            return new TxResult(false, steps, compensations,
                    "Saga编排：长事务拆分为本地事务+补偿",
                    "隐患：补偿失败会造成脏状态",
                    "优化：补偿幂等+重试队列+人工对账兜底");
        }

        steps.add("pay-success");
        steps.add("publish-order-paid-event");
        return new TxResult(true, steps, compensations,
                "最终一致性：本地事务+事件驱动",
                "隐患：消息重复与乱序",
                "优化：幂等消费+状态机校验+死信队列");
    }

    public TxResult executeTcc(boolean confirmFail) {
        List<String> steps = new ArrayList<>();
        steps.add("try-reserve-balance");
        steps.add("try-reserve-inventory");

        if (confirmFail) {
            steps.add("cancel-balance");
            steps.add("cancel-inventory");
            return new TxResult(false, steps, List.of("cancel-*"),
                    "TCC：Try/Confirm/Cancel强约束",
                    "隐患：接口侵入强、开发复杂",
                    "优化：模板化TCC框架+统一超时恢复任务");
        }

        steps.add("confirm-balance");
        steps.add("confirm-inventory");
        return new TxResult(true, steps, List.of(),
                "TCC：关键链路强一致优先",
                "隐患：性能开销高",
                "优化：只在资金类关键链路使用TCC，其它场景用Saga");
    }

    public record TxResult(
            boolean success,
            List<String> steps,
            List<String> compensations,
            String designReason,
            String risk,
            String optimization
    ) {}
}
