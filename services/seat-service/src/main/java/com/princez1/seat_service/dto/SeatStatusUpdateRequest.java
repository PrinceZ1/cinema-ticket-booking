package com.princez1.seat_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeatStatusUpdateRequest {
    private String status; // used for PATCH /api/seats/{id}/status
    private String bookingId; // optional — for lock tracking
}
