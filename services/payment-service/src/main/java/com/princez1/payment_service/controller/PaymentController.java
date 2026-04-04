package com.princez1.payment_service.controller;

import com.princez1.common_lib.response.ApiResponse;
import com.princez1.payment_service.dto.PaymentRequest;
import com.princez1.payment_service.dto.PaymentResponse;
import com.princez1.payment_service.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<ApiResponse<PaymentResponse>> initiate(@Valid @RequestBody PaymentRequest request) {
        log.debug("POST /api/payments");
        PaymentResponse created = paymentService.initiatePayment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(201, "Created", created));
    }

    @PostMapping("/{bookingId}/process")
    public ResponseEntity<ApiResponse<PaymentResponse>> process(@PathVariable String bookingId) {
        log.debug("POST /api/payments/{}/process", bookingId);
        PaymentResponse payment = paymentService.processPayment(bookingId);
        return ResponseEntity.ok(ApiResponse.success(payment));
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getByBookingId(@PathVariable String bookingId) {
        return ResponseEntity.ok(ApiResponse.success(paymentService.getPaymentByBookingId(bookingId)));
    }

    @PostMapping("/{bookingId}/refund")
    public ResponseEntity<ApiResponse<PaymentResponse>> refund(@PathVariable String bookingId) {
        PaymentResponse payment = paymentService.refundPayment(bookingId);
        return ResponseEntity.ok(ApiResponse.success(payment));
    }
}
