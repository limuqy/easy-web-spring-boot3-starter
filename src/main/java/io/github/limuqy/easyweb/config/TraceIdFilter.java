package io.github.limuqy.easyweb.config;

import io.github.limuqy.easyweb.core.util.TraceIdUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class TraceIdFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String traceId = request.getHeader(TraceIdUtil.TRACE_ID);
            if (StringUtils.isBlank(traceId)) {
                TraceIdUtil.randomTraceId();
            }
            response.setHeader(TraceIdUtil.TRACE_ID, TraceIdUtil.getTraceId());
            filterChain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
}
