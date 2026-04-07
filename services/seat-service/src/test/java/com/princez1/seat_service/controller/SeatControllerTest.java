package com.princez1.seat_service.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.princez1.seat_service.dto.SeatResponse;
import com.princez1.seat_service.exception.GlobalExceptionHandler;
import com.princez1.seat_service.exception.SeatNotAvailableException;
import com.princez1.seat_service.service.SeatService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(SeatController.class)
@Import(GlobalExceptionHandler.class)
class SeatControllerTest {

    @Autowired private MockMvc mockMvc;

    @Autowired private ObjectMapper objectMapper;

    @MockBean private SeatService seatService;

    @Test
    void testHoldSeat_returns200() throws Exception {
        Long seatId = 1L;
        String bookingId = "test-booking-1";

        SeatResponse held = new SeatResponse(seatId, 1L, "A1", "A", "HELD");

        when(seatService.holdSeat(eq(seatId), eq(bookingId))).thenReturn(held);

        mockMvc
                .perform(
                        patch("/api/seats/{id}/hold", seatId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(new BookingIdRequest(bookingId)))
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void testHoldSeat_notAvailable_returns409() throws Exception {
        Long seatId = 1L;
        String bookingId = "test-booking-1";

        when(seatService.holdSeat(eq(seatId), eq(bookingId)))
                .thenThrow(new SeatNotAvailableException("Seat " + seatId + " is currently held"));

        mockMvc
                .perform(
                        patch("/api/seats/{id}/hold", seatId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(new BookingIdRequest(bookingId)))
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void testGetSeatsByShowtime_returnsOk() throws Exception {
        Long showtimeId = 1L;

        SeatResponse seat = new SeatResponse(1L, showtimeId, "A1", "A", "AVAILABLE");

        when(seatService.getSeatsByShowtime(showtimeId)).thenReturn(List.of(seat));

        mockMvc
                .perform(get("/api/seats/showtime/{showtimeId}", showtimeId).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data[0].id").value(1));
    }

    private static class BookingIdRequest {
        private final String bookingId;

        private BookingIdRequest(String bookingId) {
            this.bookingId = bookingId;
        }

        public String getBookingId() {
            return bookingId;
        }
    }
}


