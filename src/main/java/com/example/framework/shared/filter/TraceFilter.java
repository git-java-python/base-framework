package com.example.framework.shared.filter;

import com.example.framework.shared.context.TraceContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TraceFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(TraceFilter.class);

    public static final String TRACE_ID_HEADER = "X-Trace-Id";
    public static final String SPAN_ID_HEADER  = "X-Span-Id";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String incomingTraceId = request.getHeader(TRACE_ID_HEADER);
        String parentSpanId = request.getHeader(SPAN_ID_HEADER);

        String traceId = (incomingTraceId == null || incomingTraceId.isBlank())
                ? TraceContext.newTraceId()
                : incomingTraceId;

        String spanId = TraceContext.newSpanId();

        try {
            TraceContext.setTraceId(traceId);
            TraceContext.setSpanId(spanId);
            TraceContext.setParentSpanId((parentSpanId == null || parentSpanId.isBlank()) ? null : parentSpanId);

            // 回给调用方，便于排查
            response.setHeader(TRACE_ID_HEADER, traceId);
            response.setHeader(SPAN_ID_HEADER, spanId);

            log.info("HTTP begin: method={}, uri={}", request.getMethod(), request.getRequestURI());
            filterChain.doFilter(request, response);
        } finally {
            log.info("HTTP end: method={}, uri={}, status={}",
                    request.getMethod(), request.getRequestURI(), response.getStatus());
            TraceContext.clear();
        }
    }

}
