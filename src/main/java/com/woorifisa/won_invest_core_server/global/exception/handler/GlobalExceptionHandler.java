package com.woorifisa.won_invest_core_server.global.exception.handler;

import com.woorifisa.won_invest_core_server.domain.account.exception.InvestAccountErrorCode;
import com.woorifisa.won_invest_core_server.global.exception.code.CommonErrorCode;
import com.woorifisa.won_invest_core_server.global.response.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
        return ErrorResponse.of(e.getErrorCode());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
        return ErrorResponse.of(InvestAccountErrorCode.INVALID_INPUT);
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