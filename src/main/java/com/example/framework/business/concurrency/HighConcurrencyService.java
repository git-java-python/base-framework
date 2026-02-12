package com.example.framework.business.concurrency;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class HighConcurrencyService {

    private final Semaphore semaphore = new Semaphore(64);
    private final ExecutorService pool = new ThreadPoolExecutor(
            8, 16, 60, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(200),
            new ThreadPoolExecutor.CallerRunsPolicy()
    );
    private final AtomicInteger stock = new AtomicInteger(100);

    public ConcurrencyResult flashSale(String userId, int qty) {
        boolean acquired = semaphore.tryAcquire();
        if (!acquired) {
            return new ConcurrencyResult(false, "系统繁忙，触发限流", stock.get(),
                    "削峰+隔离", "隐患：拒绝率升高", "优化：动态限流+排队令牌桶");
        }
        try {
            int remain = stock.addAndGet(-qty);
            if (remain < 0) {
                stock.addAndGet(qty);
                return new ConcurrencyResult(false, "库存不足", stock.get(),
                        "原子扣减", "隐患：热点争用", "优化：Redis+Lua扣减");
            }
            return new ConcurrencyResult(true, "下单成功 user=" + userId, remain,
                    "并发控制+原子库存", "隐患：单机内存库存不适合分布式", "优化：迁移至Redis/数据库库存中心");
        } finally {
            semaphore.release();
        }
    }

    public BatchResult processInParallel(List<Integer> payload) {
        List<CompletableFuture<Integer>> futures = new ArrayList<>();
        for (Integer value : payload) {
            futures.add(CompletableFuture.supplyAsync(() -> value * value, pool));
        }
        List<Integer> result = futures.stream().map(CompletableFuture::join).toList();
        return new BatchResult(result, "CompletableFuture并行计算", "隐患：线程池参数不当会雪崩", "优化：按业务隔离线程池并配置监控告警");
    }

    public record ConcurrencyResult(boolean success, String message, int remain,
                                    String designReason, String risk, String optimization) {}

    public record BatchResult(List<Integer> result, String designReason, String risk, String optimization) {}
}
