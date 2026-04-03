package com.princez1.customer_service.exception;

import com.princez1.common_lib.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleResourceNotFound(
            ResourceNotFoundException ex, HttpServletRequest request) {
        log.error(
                "ResourceNotFoundException traceId={}, uri={}",
                MDC.get("traceId"),
                request.getRequestURI(),
                ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(404, ex.getMessage()));
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiResponse<Object>> handleDuplicateResource(
            DuplicateResourceException ex, HttpServletRequest request) {
        log.error(
                "DuplicateResourceException traceId={}, uri={}",
                MDC.get("traceId"),
                request.getRequestURI(),
                ex);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error(409, ex.getMessage()));
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, ValidationException.class})
    public ResponseEntity<ApiResponse<Object>> handleValidationException(Exception ex) {
        log.error("ValidationException traceId={}", MDC.get("traceId"), ex);
        String message;
        if (ex instanceof MethodArgumentNotValidException manv) {
            message =
                    "Validation failed: "
                            + manv.getBindingResult().getFieldErrors().stream()
                            .map(err -> err.getField() + ": " + err.getDefaultMessage())
                            .collect(Collectors.joining(", "));
        } else {
            message = "Validation failed: " + ex.getMessage();
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(400, message));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGenericException(
            Exception ex, HttpServletRequest request) {
        log.error(
                "Unhandled exception traceId={}, uri={}",
                MDC.get("traceId"),
                request.getRequestURI(),
                ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(500, "Internal server error"));
    }
}


