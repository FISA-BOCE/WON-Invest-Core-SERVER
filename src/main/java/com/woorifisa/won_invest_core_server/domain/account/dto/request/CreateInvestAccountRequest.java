package com.woorifisa.won_invest_core_server.domain.account.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

import java.util.List;

public record CreateInvestAccountRequest(
        @NotBlank
        @Pattern(regexp = "^\\d{3}-\\d{4}-\\d{4}$", message = "전화번호 형식이 올바르지 않습니다.")
        String phoneNumber,

        @NotBlank
        String customerName,

        @NotBlank
        String accountPassword,

        @NotBlank
        String accountPasswordConfirm,

        @NotBlank
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        String email,

        @NotEmpty
        List<@NotBlank String> agreedTerms
) {}
