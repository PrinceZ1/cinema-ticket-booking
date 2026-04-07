package com.princez1.seat_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeatRequest {
    private Long showtimeId;
    private String seatNumber;
    private String row;
    private String status; // initial status when creating
}

