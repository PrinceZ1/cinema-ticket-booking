package com.princez1.seat_service.controller;

import com.princez1.common_lib.response.ApiResponse;
import com.princez1.seat_service.dto.SeatResponse;
import com.princez1.seat_service.dto.SeatStatusUpdateRequest;
import com.princez1.seat_service.service.SeatService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/seats")
@RequiredArgsConstructor
@Slf4j
public class SeatController {

    private final SeatService seatService;

    @GetMapping("/showtime/{showtimeId}")
    public ResponseEntity<ApiResponse<List<SeatResponse>>> getSeatsByShowtime(@PathVariable Long showtimeId) {
        return ResponseEntity.ok(ApiResponse.success(seatService.getSeatsByShowtime(showtimeId)));
    }

    @GetMapping("/showtime/{showtimeId}/available")
    public ResponseEntity<ApiResponse<List<SeatResponse>>> getAvailableSeatsByShowtime(@PathVariable Long showtimeId) {
        return ResponseEntity.ok(ApiResponse.success(seatService.getAvailableSeatsByShowtime(showtimeId)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SeatResponse>> getSeatById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(seatService.getSeatById(id)));
    }

    @GetMapping("/{id}/availability")
    public ResponseEntity<ApiResponse<Boolean>> getSeatAvailability(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(seatService.isSeatAvailable(id)));
    }

    @PatchMapping("/{id}/hold")
    public ResponseEntity<ApiResponse<SeatResponse>> holdSeat(
            @PathVariable Long id, @Valid @RequestBody HoldSeatRequest request) {
        SeatResponse held = seatService.holdSeat(id, request.getBookingId());
        return ResponseEntity.ok(ApiResponse.success(held));
    }

    @PatchMapping("/{id}/confirm")
    public ResponseEntity<ApiResponse<SeatResponse>> confirmSeat(
            @PathVariable Long id, @Valid @RequestBody BookingIdRequest request) {
        SeatResponse confirmed = seatService.confirmSeat(id, request.getBookingId());
        return ResponseEntity.ok(ApiResponse.success(confirmed));
    }

    @PatchMapping("/{id}/release")
    public ResponseEntity<ApiResponse<SeatResponse>> releaseSeat(
            @PathVariable Long id, @Valid @RequestBody BookingIdRequest request) {
        SeatResponse released = seatService.releaseSeat(id, request.getBookingId());
        return ResponseEntity.ok(ApiResponse.success(released));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<SeatResponse>> updateSeatStatus(
            @PathVariable Long id, @Valid @RequestBody SeatStatusUpdateRequest request) {
        // keep business logic untouched: reuse existing operations (HELD/BOOKED/AVAILABLE)
        String status = request.getStatus() != null ? request.getStatus().trim().toUpperCase() : "";
        String bookingId = request.getBookingId();
        SeatResponse updated;
        switch (status) {
            case "HELD" -> updated = seatService.holdSeat(id, bookingId);
            case "BOOKED" -> updated = seatService.confirmSeat(id, bookingId);
            case "AVAILABLE" -> updated = seatService.releaseSeat(id, bookingId);
            default -> throw new IllegalArgumentException("Unsupported status: " + request.getStatus());
        }
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    @PostMapping("/showtime/{showtimeId}/hold-multiple")
    public ResponseEntity<ApiResponse<List<SeatResponse>>> holdMultipleSeats(
            @PathVariable Long showtimeId,
            @Valid @RequestBody HoldMultipleSeatsRequest request) {
        List<SeatResponse> held = seatService.holdMultipleSeats(request.getSeatIds(), request.getBookingId());
        return ResponseEntity.ok(ApiResponse.success(held));
    }

    @PostMapping("/showtime/{showtimeId}/release-multiple")
    public ResponseEntity<ApiResponse<Void>> releaseMultipleSeats(
            @PathVariable Long showtimeId,
            @Valid @RequestBody HoldMultipleSeatsRequest request) {
        seatService.releaseMultipleSeats(request.getSeatIds(), request.getBookingId());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/showtime/{showtimeId}/confirm-multiple")
    public ResponseEntity<ApiResponse<Void>> confirmMultipleSeats(
            @PathVariable Long showtimeId,
            @Valid @RequestBody HoldMultipleSeatsRequest request) {
        seatService.confirmMultipleSeats(request.getSeatIds(), request.getBookingId());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/showtime/{showtimeId}/initialize")
    public ResponseEntity<ApiResponse<String>> initializeSeatsForShowtime(
            @PathVariable Long showtimeId, @Valid @RequestBody InitializeSeatsRequest request) {
        seatService.initializeSeatsForShowtime(showtimeId, request.getRows(), request.getSeatsPerRow());
        int total = request.getRows() * request.getSeatsPerRow();
        return ResponseEntity.ok(ApiResponse.success("Initialized " + total + " seats for showtime " + showtimeId));
    }

    @Data
    public static class BookingIdRequest {
        @NotBlank
        private String bookingId;
    }

    @Data
    public static class HoldSeatRequest {
        @NotBlank
        private String bookingId;
    }

    @Data
    public static class HoldMultipleSeatsRequest {
        @NotEmpty
        private List<Long> seatIds;
        @NotBlank
        private String bookingId;
    }

    @Data
    public static class InitializeSeatsRequest {
        @Positive
        private int rows;
        @Positive
        private int seatsPerRow;
    }
}
