package com.woorifisa.won_invest_core_server.domain.autoinvest.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;
import java.util.UUID;

public record AutoInvestExecutionRequest(
        @NotBlank
        String eventId,

        @NotBlank
        String eventType,

        @NotBlank
        String correlationId,

        @NotBlank
        String idempotencyKey,

        @NotNull
        Long sweepRequestId,

        @NotNull
        UUID userUuid,

        @NotNull
        UUID cardUserUuid,

        @NotNull
        Long performanceId,

        @NotNull
        Long pointLedgerId,

        @NotBlank
        @Pattern(regexp = "\\d{4}-(0[1-9]|1[0-2])")
        String baseMonth,

        @NotNull @Positive
        Long pointAmount,

        @NotNull @Positive
        Long krwAmount,

        @NotNull
        Long etfId,

        @NotNull
        LocalDateTime requestedAt
) {
}
