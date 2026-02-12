package com.example.framework.business.controller;

import com.example.framework.business.concurrency.HighConcurrencyService;
import com.example.framework.business.dto.CreateOrderRequest;
import com.example.framework.business.infra.InventoryService;
import com.example.framework.business.infra.OutboxService;
import com.example.framework.business.marketing.PromotionEngine;
import com.example.framework.business.microservice.MicroserviceOrderFacade;
import com.example.framework.business.middleware.MiddlewareIntegrationService;
import com.example.framework.business.notification.NotificationService;
import com.example.framework.business.order.Order;
import com.example.framework.business.order.OrderService;
import com.example.framework.business.pattern.DesignPatternPlaygroundService;
import com.example.framework.business.scenario.ScenarioSolutionService;
import com.example.framework.business.tx.DistributedTransactionService;
import com.example.framework.common.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/playbook")
@Validated
public class ArchitecturePlaybookController {

    private final OrderService orderService;
    private final PromotionEngine promotionEngine;
    private final NotificationService notificationService;
    private final ScenarioSolutionService scenarioSolutionService;
    private final OutboxService outboxService;
    private final InventoryService inventoryService;
    private final MicroserviceOrderFacade microserviceOrderFacade;
    private final HighConcurrencyService highConcurrencyService;
    private final DesignPatternPlaygroundService designPatternPlaygroundService;
    private final DistributedTransactionService distributedTransactionService;
    private final MiddlewareIntegrationService middlewareIntegrationService;

    public ArchitecturePlaybookController(OrderService orderService,
                                          PromotionEngine promotionEngine,
                                          NotificationService notificationService,
                                          ScenarioSolutionService scenarioSolutionService,
                                          OutboxService outboxService,
                                          InventoryService inventoryService,
                                          MicroserviceOrderFacade microserviceOrderFacade,
                                          HighConcurrencyService highConcurrencyService,
                                          DesignPatternPlaygroundService designPatternPlaygroundService,
                                          DistributedTransactionService distributedTransactionService,
                                          MiddlewareIntegrationService middlewareIntegrationService) {
        this.orderService = orderService;
        this.promotionEngine = promotionEngine;
        this.notificationService = notificationService;
        this.scenarioSolutionService = scenarioSolutionService;
        this.outboxService = outboxService;
        this.inventoryService = inventoryService;
        this.microserviceOrderFacade = microserviceOrderFacade;
        this.highConcurrencyService = highConcurrencyService;
        this.designPatternPlaygroundService = designPatternPlaygroundService;
        this.distributedTransactionService = distributedTransactionService;
        this.middlewareIntegrationService = middlewareIntegrationService;
    }

    @GetMapping("/scenarios")
    public ApiResponse<List<ScenarioSolutionService.ScenarioSolution>> scenarios() {
        return ApiResponse.success(scenarioSolutionService.all());
    }

    @PostMapping("/orders")
    public ApiResponse<Order> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        return ApiResponse.success(orderService.createOrder(request));
    }

    @PostMapping("/orders/{orderId}/pay")
    public ApiResponse<Order> payOrder(@PathVariable String orderId,
                                       @NotBlank @RequestParam("channelTxnId") String channelTxnId) {
        return ApiResponse.success(orderService.payOrder(orderId, channelTxnId));
    }

    @PostMapping("/orders/{orderId}/cancel")
    public ApiResponse<Order> cancelOrder(@PathVariable String orderId) {
        return ApiResponse.success(orderService.cancelOrder(orderId));
    }

    @GetMapping("/orders/{orderId}")
    public ApiResponse<Order> getOrder(@PathVariable String orderId) {
        return ApiResponse.success(orderService.getOrder(orderId));
    }

    @GetMapping("/inventory/{sku}")
    public ApiResponse<Integer> getInventory(@PathVariable String sku) {
        return ApiResponse.success(inventoryService.getAvailable(sku));
    }

    @GetMapping("/promotions/best-offer")
    public ApiResponse<PromotionEngine.PromotionResult> bestOffer(@NotNull @RequestParam("amount") BigDecimal amount) {
        return ApiResponse.success(promotionEngine.bestOffer(amount));
    }

    @PostMapping("/notifications/send")
    public ApiResponse<NotificationService.DeliveryResult> send(@NotBlank @RequestParam("bizId") String bizId,
                                                                @NotBlank @RequestParam("receiver") String receiver,
                                                                @NotBlank @RequestParam("content") String content) {
        return ApiResponse.success(notificationService.sendWithPolicy(bizId, receiver, content));
    }

    @GetMapping("/outbox")
    public ApiResponse<List<OutboxService.OutboxEvent>> outboxEvents() {
        return ApiResponse.success(outboxService.allEvents());
    }

    @GetMapping("/microservice/order-view")
    public ApiResponse<MicroserviceOrderFacade.AggregatedView> orderView(
            @RequestParam("orderId") String orderId,
            @RequestParam(value = "simulateUserFailure", defaultValue = "false") boolean simulateUserFailure) {
        return ApiResponse.success(microserviceOrderFacade.queryOrderWithUserProfile(orderId, simulateUserFailure));
    }

    @PostMapping("/concurrency/flash-sale")
    public ApiResponse<HighConcurrencyService.ConcurrencyResult> flashSale(
            @RequestParam("userId") String userId,
            @RequestParam(value = "qty", defaultValue = "1") int qty) {
        return ApiResponse.success(highConcurrencyService.flashSale(userId, qty));
    }

    @PostMapping("/concurrency/parallel-square")
    public ApiResponse<HighConcurrencyService.BatchResult> parallelSquare(@RequestBody List<Integer> payload) {
        return ApiResponse.success(highConcurrencyService.processInParallel(payload));
    }

    @GetMapping("/patterns/pay")
    public ApiResponse<DesignPatternPlaygroundService.PatternResult> patternPay(
            @RequestParam("payType") String payType,
            @RequestParam("amount") BigDecimal amount,
            @RequestParam(value = "vip", defaultValue = "false") boolean vip) {
        return ApiResponse.success(designPatternPlaygroundService.run(payType, amount, vip));
    }

    @PostMapping("/tx/saga")
    public ApiResponse<DistributedTransactionService.TxResult> saga(
            @RequestParam(value = "paymentFail", defaultValue = "false") boolean paymentFail) {
        return ApiResponse.success(distributedTransactionService.executeSaga(paymentFail));
    }

    @PostMapping("/tx/tcc")
    public ApiResponse<DistributedTransactionService.TxResult> tcc(
            @RequestParam(value = "confirmFail", defaultValue = "false") boolean confirmFail) {
        return ApiResponse.success(distributedTransactionService.executeTcc(confirmFail));
    }

    @GetMapping("/middleware/cache-product")
    public ApiResponse<MiddlewareIntegrationService.MiddlewareResult> cacheProduct(@RequestParam("sku") String sku) {
        return ApiResponse.success(middlewareIntegrationService.queryProduct(sku));
    }

    @PostMapping("/middleware/publish-order-event")
    public ApiResponse<MiddlewareIntegrationService.MiddlewareResult> publishOrderEvent(@RequestParam("orderId") String orderId) {
        return ApiResponse.success(middlewareIntegrationService.publishOrderEvent(orderId));
    }

    @PostMapping("/middleware/lock-execute")
    public ApiResponse<MiddlewareIntegrationService.MiddlewareResult> lockExecute(@RequestParam("resource") String resource) {
        return ApiResponse.success(middlewareIntegrationService.lockAndExecute(resource));
    }
}
