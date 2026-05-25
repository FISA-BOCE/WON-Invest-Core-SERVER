package com.woorifisa.won_invest_core_server.global.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.woorifisa.won_invest_core_server.global.exception.code.CommonErrorCode;
import com.woorifisa.won_invest_core_server.global.exception.handler.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
public class JwtUtil {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public UUID extractUserUuid(String bearerToken) {
        if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
            throw new BusinessException(CommonErrorCode.UNAUTHORIZED);
        }
        String token = bearerToken.substring(7);
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new BusinessException(CommonErrorCode.UNAUTHORIZED);
        }
        try {
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
            Map<?, ?> claims = objectMapper.readValue(payload, Map.class);
            String userUuidStr = (String) claims.get("user_uuid");
            if (userUuidStr == null || userUuidStr.isBlank()) {
                throw new BusinessException(CommonErrorCode.UNAUTHORIZED);
            }
            return UUID.fromString(userUuidStr);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.warn("JWT 파싱 실패: {}", e.getMessage());
            throw new BusinessException(CommonErrorCode.UNAUTHORIZED);
        }
    }
}