package com.princez1.payment_service.service.impl;

import com.princez1.payment_service.dto.PaymentRequest;
import com.princez1.payment_service.dto.PaymentResponse;
import com.princez1.payment_service.entity.Payment;
import com.princez1.payment_service.exception.InvalidPaymentStateException;
import com.princez1.payment_service.exception.ResourceNotFoundException;
import com.princez1.payment_service.repository.PaymentRepository;
import com.princez1.payment_service.service.PaymentService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;

    @Override
    @Transactional
    public PaymentResponse initiatePayment(PaymentRequest request) {
        log.debug("initiatePayment(bookingId={})", request.getBookingId());

        Payment payment = toEntity(request);
        payment.setStatus(Payment.PaymentStatus.PENDING);

        return toResponse(paymentRepository.save(payment));
    }

    @Override
    @Transactional
    public PaymentResponse processPayment(String bookingId) {
        log.debug("processPayment(bookingId={})", bookingId);

        Payment payment =
                paymentRepository
                        .findByBookingId(bookingId)
                        .orElseThrow(() -> new ResourceNotFoundException("Payment", bookingId));

        if (payment.getStatus() != Payment.PaymentStatus.PENDING) {
            throw new InvalidPaymentStateException(
                    "Payment cannot be processed in state: " + payment.getStatus());
        }

        boolean success = new Random().nextInt(100) < 80;
        if (success) {
            payment.setStatus(Payment.PaymentStatus.SUCCESS);
            payment.setPaidAt(LocalDateTime.now());
            payment.setFailureReason(null);
        } else {
            payment.setStatus(Payment.PaymentStatus.FAILED);
            payment.setFailureReason("Insufficient funds (simulated)");
            payment.setPaidAt(null);
        }

        return toResponse(paymentRepository.save(payment));
    }

    @Override
    @Transactional
    public PaymentResponse getPaymentByBookingId(String bookingId) {
        log.debug("getPaymentByBookingId(bookingId={})", bookingId);
        return toResponse(
                paymentRepository
                        .findByBookingId(bookingId)
                        .orElseThrow(() -> new ResourceNotFoundException("Payment", bookingId)));
    }

    @Override
    @Transactional
    public PaymentResponse refundPayment(String bookingId) {
        log.debug("refundPayment(bookingId={})", bookingId);

        Payment payment =
                paymentRepository
                        .findByBookingId(bookingId)
                        .orElseThrow(() -> new ResourceNotFoundException("Payment", bookingId));

        if (payment.getStatus() != Payment.PaymentStatus.SUCCESS) {
            throw new InvalidPaymentStateException(
                    "Refund is only allowed when payment status is SUCCESS");
        }

        payment.setStatus(Payment.PaymentStatus.REFUNDED);
        return toResponse(paymentRepository.save(payment));
    }

    private PaymentResponse toResponse(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getBookingId(),
                payment.getAmount(),
                payment.getStatus() != null ? payment.getStatus().name() : null,
                payment.getPaidAt());
    }

    private Payment toEntity(PaymentRequest request) {
        return Payment.builder().bookingId(request.getBookingId()).amount(request.getAmount()).build();
    }
}

