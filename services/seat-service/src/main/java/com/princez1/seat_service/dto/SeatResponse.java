package com.princez1.seat_service.dto;

public record SeatResponse(
        Long id,
        Long showtimeId,
        String seatNumber,
        String row,
        String status) {}
