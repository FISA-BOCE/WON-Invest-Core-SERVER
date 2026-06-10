package com.woorifisa.won_invest_core_server.global.config;

import com.woorifisa.won_invest_core_server.global.security.InternalApiAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(InternalAuthProperties.class)
public class SecurityConfig {

    // 내부 API 인증 검사 필터 주입
    private final InternalApiAuthFilter internalApiAuthFilter;

    // WON-Invest-Core-SERVER 보안 규칙
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                // 브라우저 폼 로그인 기반 CSRF 보호는 사용하지 않음
                .csrf(AbstractHttpConfigurer::disable)
                // Spring Security 기본 로그인 기능 끔
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                // 세션 사용 안 함 - 요청 올 때마다 인증 헤더 확인하기
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-ui.html"
                        ).permitAll()
                        .requestMatchers("/actuator/health", "/actuator/info", "/actuator/prometheus").permitAll()
                        // '/internal/**' 요청은 인증 필요
                        .requestMatchers("/internal/**").hasRole("INTERNAL")
                        // 명시적으로 허용한 경로 외에는 차단
                        .anyRequest().denyAll()
                )
                // Spring Security의 기본 로그인 인증 필터보다 먼저 -> InternalApiAuthFilter를 실행
                .addFilterBefore(
                        internalApiAuthFilter,
                        UsernamePasswordAuthenticationFilter.class
                )
                .build();
    }
}
