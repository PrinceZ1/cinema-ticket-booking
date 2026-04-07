package com.princez1.seat_service.service;

import com.princez1.seat_service.dto.SeatResponse;

import java.util.List;

public interface SeatService {

    // Query
    List<SeatResponse> getSeatsByShowtime(Long showtimeId);

    List<SeatResponse> getAvailableSeatsByShowtime(Long showtimeId);

    SeatResponse getSeatById(Long id);

    boolean isSeatAvailable(Long seatId);

    // Core lock operations
    SeatResponse holdSeat(Long seatId, String bookingId);

    SeatResponse confirmSeat(Long seatId, String bookingId);

    SeatResponse releaseSeat(Long seatId, String bookingId);

    // Bulk operations (used by booking-service)
    List<SeatResponse> holdMultipleSeats(List<Long> seatIds, String bookingId);

    void releaseMultipleSeats(List<Long> seatIds, String bookingId);

    void confirmMultipleSeats(List<Long> seatIds, String bookingId);

    // Maintenance
    void releaseExpiredHeldSeats();

    // Seed data helper
    void initializeSeatsForShowtime(Long showtimeId, int rows, int seatsPerRow);
}

