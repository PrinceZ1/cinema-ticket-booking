package com.princez1.payment_service.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentResponse(
        Long id,
        String bookingId,
        BigDecimal amount,
        String status,
        LocalDateTime paidAt) {}
