package com.woorifisa.won_invest_core_server.global.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class HttpLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        long startTime = System.currentTimeMillis();
        boolean loggedOnException = false;
        try {
            filterChain.doFilter(request, response);
        } catch (Exception ex) {
            long elapsed = System.currentTimeMillis() - startTime;
            int status = response.getStatus();
            if (status < 400) {
                status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
            }
            log.error("http error method={} uri={} status={} elapsed_ms={}",
                    request.getMethod(), request.getRequestURI(), status, elapsed, ex);
            loggedOnException = true;
            throw ex;
        } finally {
            int status = response.getStatus();
            if (!loggedOnException && status >= 400) {
                long elapsed = System.currentTimeMillis() - startTime;
                String method = request.getMethod();
                String uri = request.getRequestURI();
                if (status >= 500) {
                    log.error("http error method={} uri={} status={} elapsed_ms={}", method, uri, status, elapsed);
                } else {
                    log.warn("http error method={} uri={} status={} elapsed_ms={}", method, uri, status, elapsed);
                }
            }
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri.startsWith("/actuator") || uri.startsWith("/swagger-ui") || uri.startsWith("/v3/api-docs");
    }
}
