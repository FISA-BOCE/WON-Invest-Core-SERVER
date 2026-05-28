package com.woorifisa.won_invest_core_server.global.exception.handler;


import com.woorifisa.won_invest_core_server.global.exception.code.ErrorCode;
import com.woorifisa.won_invest_core_server.global.response.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.woorifisa.won_invest_core_server.global.exception.code.CommonErrorCode;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
        return ErrorResponse.of(e.getErrorCode());
    }

    // 400 반환
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException exception
    ) {
        return ErrorResponse.of(CommonErrorCode.INVALID_INPUT_VALUE);
    }

    // HttpMessageNotReadableException도 같이 잡는 이유 - JSON 형식이 잘못됐거나 enum 변환이 실패할 때도 400 공통 응답으로 내려주기 위해서
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException exception
    ) {
        return ErrorResponse.of(CommonErrorCode.INVALID_INPUT_VALUE);
    }

    @ExceptionHandler({MissingRequestHeaderException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ErrorResponse> handleBadRequestException(Exception e) {
        return ErrorResponse.of(CommonErrorCode.INVALID_INPUT_VALUE);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        return ErrorResponse.of(CommonErrorCode.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        return ErrorResponse.of(CommonErrorCode.INTERNAL_SERVER_ERROR);
    }
}
