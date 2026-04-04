package com.princez1.payment_service.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.Map;

import com.princez1.payment_service.dto.PaymentRequest;
import com.princez1.payment_service.dto.PaymentResponse;
import com.princez1.payment_service.exception.GlobalExceptionHandler;
import com.princez1.payment_service.exception.ResourceNotFoundException;
import com.princez1.payment_service.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PaymentController.class)
@Import(GlobalExceptionHandler.class)
class PaymentControllerTest {

    @Autowired private MockMvc mockMvc;

    @Autowired private ObjectMapper objectMapper;

    @MockBean private PaymentService paymentService;

    @Test
    void testInitiatePayment_validInput_returns201() throws Exception {
        String bookingId = "booking-uuid-1";
        BigDecimal amount = new BigDecimal("150000");

        PaymentResponse created = new PaymentResponse(1L, bookingId, amount, "PENDING", null);

        when(paymentService.initiatePayment(any(PaymentRequest.class))).thenReturn(created);

        Map<String, Object> request = Map.of("bookingId", bookingId, "amount", amount);

        mockMvc
                .perform(
                        post("/api/payments")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.data.bookingId").value(bookingId));
    }

    @Test
    void testGetPayment_notFound_returns404() throws Exception {
        String bookingId = "booking-uuid-missing";

        when(paymentService.getPaymentByBookingId(bookingId))
                .thenThrow(new ResourceNotFoundException("Payment", bookingId));

        mockMvc
                .perform(get("/api/payments/{bookingId}", bookingId).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void testProcessPayment_returns200() throws Exception {
        String bookingId = "booking-uuid-2";

        PaymentResponse processed =
                new PaymentResponse(2L, bookingId, new BigDecimal("1000"), "SUCCESS", null);

        when(paymentService.processPayment(bookingId)).thenReturn(processed);

        mockMvc
                .perform(
                        post("/api/payments/{bookingId}/process", bookingId).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.status").value("SUCCESS"));
    }
}

