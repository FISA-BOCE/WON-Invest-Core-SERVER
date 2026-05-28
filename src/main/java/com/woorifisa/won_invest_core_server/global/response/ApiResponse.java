package com.woorifisa.won_invest_core_server.global.response;

public record ApiResponse<T>(
        int status,
        String code,
        String msg,
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

    public static <T> ApiResponse<T> success(String msg, T data) {
        return new ApiResponse<>(
                200,
                "SUCCESS",
                msg,
                data
        );
    }
}