package com.princez1.seat_service.exception;

import com.princez1.common_lib.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(SeatNotAvailableException.class)
    public ResponseEntity<ApiResponse<Object>> handleSeatNotAvailable(
            SeatNotAvailableException ex, HttpServletRequest request) {
        log.error(
                "SeatNotAvailableException traceId={}, uri={}",
                MDC.get("traceId"),
                request.getRequestURI(),
                ex);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error(409, ex.getMessage()));
    }

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

    @ExceptionHandler(InvalidSeatOperationException.class)
    public ResponseEntity<ApiResponse<Object>> handleInvalidSeatOperation(
            InvalidSeatOperationException ex, HttpServletRequest request) {
        log.error(
                "InvalidSeatOperationException traceId={}, uri={}",
                MDC.get("traceId"),
                request.getRequestURI(),
                ex);
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ApiResponse.error(422, ex.getMessage()));
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ApiResponse<Object>> handleOptimisticLocking(
            ObjectOptimisticLockingFailureException ex, HttpServletRequest request) {
        log.error(
                "OptimisticLockingFailure traceId={}, uri={}",
                MDC.get("traceId"),
                request.getRequestURI(),
                ex);
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(409, "Seat was modified concurrently, please retry"));
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

