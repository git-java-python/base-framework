package com.example.framework.business.advanced;

import com.example.framework.business.concurrency.HighConcurrencyService;
import com.example.framework.business.microservice.MicroserviceOrderFacade;
import com.example.framework.business.pattern.DesignPatternPlaygroundService;
import com.example.framework.business.tx.DistributedTransactionService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AdvancedPlaybookTest {

    @Test
    void shouldFallbackWhenMicroserviceFailure() {
        MicroserviceOrderFacade facade = new MicroserviceOrderFacade();
        var result = facade.queryOrderWithUserProfile("o-1", true);
        assertEquals("DEGRADED", result.user().get("riskTag"));
    }

    @Test
    void shouldProcessParallelPayload() {
        HighConcurrencyService service = new HighConcurrencyService();
        var result = service.processInParallel(List.of(1, 2, 3));
        assertEquals(List.of(1, 4, 9), result.result());
    }

    @Test
    void shouldRunPatternAndTxDemo() {
        DesignPatternPlaygroundService patternService = new DesignPatternPlaygroundService();
        var pattern = patternService.run("ALIPAY", new BigDecimal("100"), false);
        assertNotNull(pattern.payload());

        DistributedTransactionService tx = new DistributedTransactionService();
        var sagaFail = tx.executeSaga(true);
        assertFalse(sagaFail.success());
        assertTrue(sagaFail.compensations().contains("cancel-order"));
    }
}
