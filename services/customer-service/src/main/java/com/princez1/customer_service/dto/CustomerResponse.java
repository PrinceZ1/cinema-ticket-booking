package com.princez1.customer_service.dto;

public record CustomerResponse(
        Long id,
        String fullName,
        String phone,
        String email) {}

