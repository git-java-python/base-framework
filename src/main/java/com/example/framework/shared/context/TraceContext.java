package com.example.framework.shared.context;

import org.slf4j.MDC;

import java.util.UUID;

/**
 * 服务调用链路追踪
 */
public class TraceContext {

    //调用入口生成全局链路 id 如果跨服务使用MDC
    public static final String TRACE_ID = "traceId";
    //每个服务调用生成自己的 spanId
    public static final String SPAN_ID = "spanId";
    public static final String PARENT_SPAN_ID = "parentSpanId";

    private static final ThreadLocal<Ctx> CTX =   ThreadLocal.withInitial(Ctx::new);

    public TraceContext() {
    }

    private static final class Ctx {
        private String traceId;
        private String spanId;
        private String parentSpanId;

        public Ctx() {}

        public Ctx(String traceId, String spanId, String parentSpanId) {
            this.traceId = traceId;
            this.spanId = spanId;
            this.parentSpanId = parentSpanId;
        }
        public String getTraceId() { return traceId; }
        public String getSpanId() { return spanId; }
        public String getParentSpanId() { return parentSpanId; }
    }

    // ===== getters =====
    public static String traceId() { return CTX.get().traceId; }
    public static String spanId() { return CTX.get().spanId; }
    public static String parentSpanId() { return CTX.get().parentSpanId; }

    // ===== setters (null safe) =====
    public static void setTraceId(String traceId) {
        CTX.get().traceId = traceId;
        putOrRemove(TRACE_ID, traceId);
    }

    public static void setSpanId(String spanId) {
        CTX.get().spanId = spanId;
        putOrRemove(SPAN_ID, spanId);
    }

    public static void setParentSpanId(String parentSpanId) {
        CTX.get().parentSpanId = parentSpanId;
        putOrRemove(PARENT_SPAN_ID, parentSpanId);
    }

    public static void setAll(String traceId, String spanId, String parentSpanId) {
        setTraceId(traceId);
        setSpanId(spanId);
        setParentSpanId(parentSpanId);
    }

    /** 复制当前线程上下文，用于异步传递 */
    public static Ctx snapshot() {
        Ctx c = CTX.get();
        return new Ctx(c.traceId, c.spanId, c.parentSpanId);
    }

    /** 用 snapshot 恢复上下文 */
    public static void restore(Ctx snapshot) {
        if (snapshot == null) {
            clear();
            return;
        }
        setAll(snapshot.traceId, snapshot.spanId, snapshot.parentSpanId);
    }

    /** 只清理我们自己放的 MDC key，不要 MDC.clear() */
    public static void clear() {
        CTX.remove();
        MDC.remove(TRACE_ID);
        MDC.remove(SPAN_ID);
        MDC.remove(PARENT_SPAN_ID);
    }

    private static void putOrRemove(String key, String val) {
        if (val == null || val.isBlank()) {
            MDC.remove(key);
        } else {
            MDC.put(key, val);
        }
    }

    // ===== id generators =====
    public static String newTraceId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public static String newSpanId() {
        // 16 chars
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

}
