// /internal/** 요청에 대해 X-Service-ID와 X-Internal-Api-Key 헤더를 검사해서
//맞으면 인증 처리 (ROLE_INTERNAL 인증 객체를 SecurityContext에 등록해 Controller까지 통과)
//틀리면 401 Unauthorized로 차단하는 필터

package com.woorifisa.won_invest_core_server.global.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.woorifisa.won_invest_core_server.global.config.InternalAuthProperties;
import com.woorifisa.won_invest_core_server.global.response.ErrorCode;
import com.woorifisa.won_invest_core_server.global.response.ErrorResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

@Component
@RequiredArgsConstructor
// OncePerRequestFilter: 요청 한 번당 한 번만 실행되는 필터
public class InternalApiAuthFilter extends OncePerRequestFilter {

    private static final String SERVICE_ID_HEADER = "X-Service-ID";
    private static final String API_KEY_HEADER = "X-Internal-Api-Key";

    // InternalAuthProperties - application.yml의 이 값을 읽어오는 설정 객체
    private final InternalAuthProperties internalAuthProperties;
    // Java 객체를 JSON으로 바꿔주는 Jackson 객체 - 인증 실패 시 ErrorResponse 객체를 JSON으로 변환해서 응답에 써줄 때 사용
    private final ObjectMapper objectMapper;

    // URI가 '/internal/'로 시작하지 않으면 필터를 적용하지 않음
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // getServletPath: context path를 제외한 실제 애플리케이션 내부 경로 기준
        String path = request.getServletPath();

        return !(path.equals("/internal") || path.startsWith("/internal/"));
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
        if (!isValidInternalRequest(serviceId, apiKey)) {
            writeErrorResponse(response, ErrorCode.AUTH_401_001);
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

    // 내부 요청 검증 메서드
    private boolean isValidInternalRequest(String serviceId, String apiKey) {
        return constantTimeEquals(internalAuthProperties.serviceId(), serviceId)
                && constantTimeEquals(internalAuthProperties.apiKey(), apiKey);
    }

    // constant-time 비교
    private boolean constantTimeEquals(String expected, String actual) {
        if (expected == null || actual == null) {
            return false;
        }

        byte[] expectedBytes = expected.getBytes(StandardCharsets.UTF_8);
        byte[] actualBytes = actual.getBytes(StandardCharsets.UTF_8);

        return MessageDigest.isEqual(expectedBytes, actualBytes);
    }

    // 인증 실패 시 공통 에러 응답을 만들어 내려주는 메서드
    private void writeErrorResponse(
            HttpServletResponse response,
            ErrorCode errorCode
    ) throws IOException {
        response.setStatus(errorCode.getHttpStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        ErrorResponse errorResponse = ErrorResponse.from(errorCode);
        objectMapper.writeValue(response.getWriter(), errorResponse);
    }
}
