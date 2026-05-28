package com.woorifisa.won_invest_core_server.global.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

// api-key가 비어 있거나 없으면 서버가 아예 뜨지 않음
@Validated
@ConfigurationProperties(prefix = "internal.auth")
public record InternalAuthProperties(

        @NotBlank(message = "internal.auth.service-id 설정은 필수입니다.")
        String serviceId,

        @NotBlank(message = "internal.auth.api-key 설정은 필수입니다.")
        String apiKey
) {
}