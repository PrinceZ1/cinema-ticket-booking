package com.princez1.payment_service.service;

import com.princez1.payment_service.dto.PaymentRequest;
import com.princez1.payment_service.dto.PaymentResponse;

public interface PaymentService {

    PaymentResponse initiatePayment(PaymentRequest request);

    PaymentResponse processPayment(String bookingId);

    PaymentResponse getPaymentByBookingId(String bookingId);

    PaymentResponse refundPayment(String bookingId);
}
