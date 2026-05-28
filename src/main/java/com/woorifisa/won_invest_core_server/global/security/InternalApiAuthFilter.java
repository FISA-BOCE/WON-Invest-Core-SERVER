// /internal/** 요청에 대해 X-Service-ID와 X-Internal-Api-Key 헤더를 검사해서
//맞으면 인증 처리 (ROLE_INTERNAL 인증 객체를 SecurityContext에 등록해 Controller까지 통과)
//틀리면 401 Unauthorized로 차단하는 필터

package com.woorifisa.won_invest_core_server.global.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
// OncePerRequestFilter: 요청 한 번당 한 번만 실행되는 필터
public class InternalApiAuthFilter extends OncePerRequestFilter {

    private static final String SERVICE_ID_HEADER = "X-Service-ID";
    private static final String API_KEY_HEADER = "X-Internal-Api-Key";

    // application.yml 에서 값 가져오기
    @Value("${internal.auth.service-id:won-channel}")
    private String expectedServiceId;

    @Value("${internal.auth.api-key:local-dev-internal-key}")
    private String expectedApiKey;

    // URI가 '/internal/'로 시작하지 않으면 필터를 적용하지 않음
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/internal/");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String serviceId = request.getHeader(SERVICE_ID_HEADER);
        String apiKey = request.getHeader(API_KEY_HEADER);

        // serviceId가 기대값과 다르거나, apiKey가 기대값과 다르면 -> 인증 실패
        if (!expectedServiceId.equals(serviceId) || !expectedApiKey.equals(apiKey)) {
            // 인증 실패 시 401 Unauthorized 응답
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"message\":\"Unauthorized internal API request\"}");
            return;
        }

        // 인증 성공 시
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        serviceId,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_INTERNAL"))
                );

        // SecurityContext에 인증 정보 저장
        SecurityContextHolder.getContext().setAuthentication(authentication);
        filterChain.doFilter(request, response);
    }
}