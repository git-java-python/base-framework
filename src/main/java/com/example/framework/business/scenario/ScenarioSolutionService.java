package com.example.framework.business.scenario;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ScenarioSolutionService {

    public List<ScenarioSolution> all() {
        return List.of(
                new ScenarioSolution(
                        "高并发下单防重与防超卖",
                        "幂等键 + 本地库存预占 + 状态机推进 + outbox事件",
                        "避免重复下单与并发库存扣减冲突，同时保证事件最终可达",
                        "本地内存幂等在多实例下不完全可靠，需升级到Redis/DB唯一约束",
                        "将幂等存储升级为 Redis SETNX + 过期时间，库存升级为 Redis + Lua 原子扣减",
                        "OrderService#createOrder / InventoryService#reserve"
                ),
                new ScenarioSolution(
                        "支付回调乱序与重复",
                        "以 channelTxnId 作为幂等主键，重复回调直接返回",
                        "三方支付平台通常至少一次投递，幂等是必需",
                        "若不校验状态机，可能将已取消订单推进为已支付",
                        "回调处理前增加状态校验和签名校验，并记录回调审计日志",
                        "OrderService#payOrder"
                ),
                new ScenarioSolution(
                        "微服务下游故障扩散",
                        "聚合服务 + 熔断降级返回兜底用户画像",
                        "控制故障传播范围，保证主流程可用",
                        "降级数据不准确可能影响业务决策",
                        "增加舱壁隔离、重试预算、服务网格治理",
                        "MicroserviceOrderFacade + CircuitBreaker"
                ),
                new ScenarioSolution(
                        "多线程批处理与线程池治理",
                        "CompletableFuture并行计算 + 有界线程池",
                        "提高吞吐并防止无界队列耗尽内存",
                        "线程池参数错误会导致排队飙升",
                        "按业务隔离线程池并暴露监控指标",
                        "HighConcurrencyService#processInParallel"
                ),
                new ScenarioSolution(
                        "设计模式治理复杂业务分支",
                        "策略模式 + 工厂模式 + 责任链",
                        "扩展支付与校验逻辑时减少if-else膨胀",
                        "模式滥用会提升理解成本",
                        "通过SPI和配置中心驱动策略装配",
                        "DesignPatternPlaygroundService#run"
                ),
                new ScenarioSolution(
                        "分布式事务一致性",
                        "Saga（默认）+ TCC（资金强一致）",
                        "在一致性与性能间做分层取舍",
                        "补偿失败会造成中间态",
                        "补偿幂等、重试队列、死信与人工对账闭环",
                        "DistributedTransactionService#executeSaga/executeTcc"
                ),
                new ScenarioSolution(
                        "中间件落地：缓存、MQ、分布式锁",
                        "缓存前置 + MQ解耦 + 锁保护临界资源",
                        "提升性能并解耦核心链路",
                        "缓存击穿、消息重复、锁误释放",
                        "布隆过滤器、幂等消费、看门狗续期与超时兜底",
                        "MiddlewareIntegrationService"
                ),
                new ScenarioSolution(
                        "营销策略频繁变化",
                        "规则接口抽象 + 可插拔规则实现 + 最优优惠选择",
                        "规则外置后可快速迭代活动，不必频繁发布",
                        "规则数量增长后计算复杂度上升，可能影响结算RT",
                        "对热门场景做预计算，复杂组合引入专用优化器",
                        "PromotionEngine#bestOffer"
                ),
                new ScenarioSolution(
                        "消息通知防骚扰",
                        "去重+频控双策略",
                        "避免重复消息和短时间轰炸用户",
                        "仅本地内存频控在分布式环境下会失效",
                        "频控迁移至Redis滑动窗口并引入多供应商路由",
                        "NotificationService#sendWithPolicy"
                )
        );
    }

    public record ScenarioSolution(
            String scenario,
            String design,
            String why,
            String risk,
            String optimization,
            String implementation
    ) {}
}
