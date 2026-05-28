package com.woorifisa.won_invest_core_server.global.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.http.ResponseEntity;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        int status,
        String message,
        T data
) {
    public static <T> ResponseEntity<ApiResponse<T>> of(SuccessStatus successStatus, T data) {
        return ResponseEntity
                .status(successStatus.getHttpStatus())
                .body(new ApiResponse<>(successStatus.getStatusCode(), successStatus.getMessage(), data));
    }

    public static <T> ResponseEntity<ApiResponse<T>> of(SuccessStatus successStatus, String message, T data) {
        return ResponseEntity
                .status(successStatus.getHttpStatus())
                .body(new ApiResponse<>(successStatus.getStatusCode(), message, data));
    }
}
