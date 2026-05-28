package com.woorifisa.won_invest_core_server.global.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        int status,

        String code,
        String message,
        T data
) {
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(
                200,
                "SUCCESS",
                "요청이 정상 처리되었습니다.",
                data
        );
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(
                200,
                "SUCCESS",
                message,
                data
        );
    }

    public static <T> ApiResponse<T> of(SuccessStatus successStatus, T data) {
        return new ApiResponse<>(
                successStatus.getStatusCode(),
                "SUCCESS",
                successStatus.getMessage(),
                data
        );
    }

    public static <T> ApiResponse<T> of(SuccessStatus successStatus, String message, T data) {
        return new ApiResponse<>(
                successStatus.getStatusCode(),
                "SUCCESS",
                message,
                data
        );
    }
}