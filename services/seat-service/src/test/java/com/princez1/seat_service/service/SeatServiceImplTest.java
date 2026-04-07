package com.princez1.seat_service.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.princez1.common_lib.redis.RedisUtil;
import com.princez1.seat_service.dto.SeatResponse;
import com.princez1.seat_service.entity.Seat;
import com.princez1.seat_service.entity.SeatStatus;
import com.princez1.seat_service.exception.InvalidSeatOperationException;
import com.princez1.seat_service.exception.SeatNotAvailableException;
import com.princez1.seat_service.repository.SeatRepository;
import com.princez1.seat_service.service.impl.SeatServiceImpl;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SeatServiceImplTest {

    private static final long LOCK_TTL_MINUTES = 10L;

    @Mock private SeatRepository seatRepository;

    @Mock private RedisUtil redisUtil;

    @InjectMocks private SeatServiceImpl seatService;

    @Test
    void testHoldSeat_available_success() {
        Long seatId = 1L;
        String bookingId = "booking-1";

        String lockKey = String.format("seat:lock:%d", seatId);
        String statusKey = String.format("seat:status:%d", seatId);

        Seat seat =
                Seat.builder()
                        .id(seatId)
                        .showtimeId(1L)
                        .seatNumber("A1")
                        .row("A")
                        .seatIndex(1)
                        .status(SeatStatus.AVAILABLE)
                        .heldByBookingId(null)
                        .heldUntil(null)
                        .version(1L)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();

        when(redisUtil.setIfAbsent(eq(lockKey), eq(bookingId), eq(LOCK_TTL_MINUTES), eq(TimeUnit.MINUTES)))
                .thenReturn(true);
        when(seatRepository.findById(seatId)).thenReturn(Optional.of(seat));
        when(seatRepository.save(any(Seat.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        SeatResponse held = seatService.holdSeat(seatId, bookingId);

        assertEquals("HELD", held.status());
        verify(redisUtil).set(eq(statusKey), eq("HELD"), eq(LOCK_TTL_MINUTES), eq(TimeUnit.MINUTES));
        verify(redisUtil, never()).delete(eq(lockKey));
    }

    @Test
    void testHoldSeat_redisLockFailed_throwsSeatNotAvailable() {
        Long seatId = 1L;
        String bookingId = "booking-1";

        String lockKey = String.format("seat:lock:%d", seatId);

        when(redisUtil.setIfAbsent(eq(lockKey), eq(bookingId), eq(LOCK_TTL_MINUTES), eq(TimeUnit.MINUTES)))
                .thenReturn(false);

        assertThrows(SeatNotAvailableException.class, () -> seatService.holdSeat(seatId, bookingId));

        verify(seatRepository, never()).findById(seatId);
        verify(seatRepository, never()).save(any(Seat.class));
        verify(redisUtil, never()).delete(any(String.class));
    }

    @Test
    void testHoldSeat_alreadyHeld_throwsSeatNotAvailable() {
        Long seatId = 1L;
        String bookingId = "booking-1";

        String lockKey = String.format("seat:lock:%d", seatId);
        String statusKey = String.format("seat:status:%d", seatId);

        Seat seat =
                Seat.builder()
                        .id(seatId)
                        .showtimeId(1L)
                        .seatNumber("A1")
                        .row("A")
                        .seatIndex(1)
                        .status(SeatStatus.HELD)
                        .heldByBookingId("another-booking")
                        .heldUntil(LocalDateTime.now().plusMinutes(5))
                        .version(1L)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();

        when(redisUtil.setIfAbsent(eq(lockKey), eq(bookingId), eq(LOCK_TTL_MINUTES), eq(TimeUnit.MINUTES)))
                .thenReturn(true);
        when(seatRepository.findById(seatId)).thenReturn(Optional.of(seat));

        assertThrows(SeatNotAvailableException.class, () -> seatService.holdSeat(seatId, bookingId));

        verify(redisUtil).delete(eq(lockKey));
        verify(redisUtil, never()).set(eq(statusKey), eq("HELD"), any(Long.class), eq(TimeUnit.MINUTES));
        verify(seatRepository, never()).save(any(Seat.class));
    }

    @Test
    void testReleaseSeat_wrongBookingId_throwsInvalidOperation() {
        Long seatId = 1L;
        String bookingA = "booking-A";
        String bookingB = "booking-B";

        Seat seat =
                Seat.builder()
                        .id(seatId)
                        .showtimeId(1L)
                        .seatNumber("A1")
                        .row("A")
                        .seatIndex(1)
                        .status(SeatStatus.HELD)
                        .heldByBookingId(bookingA)
                        .heldUntil(LocalDateTime.now().plusMinutes(5))
                        .version(1L)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();

        when(seatRepository.findById(seatId)).thenReturn(Optional.of(seat));

        assertThrows(
                InvalidSeatOperationException.class, () -> seatService.releaseSeat(seatId, bookingB));

        verify(seatRepository, never()).save(any(Seat.class));
        verify(redisUtil, never()).delete(any(String.class));
    }

    @Test
    void testHoldMultipleSeats_secondSeatFails_rollsBackFirst() {
        List<Long> seatIds = List.of(1L, 2L);
        String bookingId = "booking-1";

        String lockKey1 = String.format("seat:lock:%d", 1L);
        String statusKey1 = String.format("seat:status:%d", 1L);

        Seat seat1 =
                Seat.builder()
                        .id(1L)
                        .showtimeId(1L)
                        .seatNumber("A1")
                        .row("A")
                        .seatIndex(1)
                        .status(SeatStatus.AVAILABLE)
                        .heldByBookingId(null)
                        .heldUntil(null)
                        .version(1L)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();

        when(redisUtil.setIfAbsent(eq(lockKey1), eq(bookingId), eq(LOCK_TTL_MINUTES), eq(TimeUnit.MINUTES)))
                .thenReturn(true);
        when(redisUtil.setIfAbsent(eq("seat:lock:2"), eq(bookingId), eq(LOCK_TTL_MINUTES), eq(TimeUnit.MINUTES)))
                .thenReturn(false);

        when(seatRepository.findById(1L)).thenReturn(Optional.of(seat1));
        when(seatRepository.save(any(Seat.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        assertThrows(SeatNotAvailableException.class, () -> seatService.holdMultipleSeats(seatIds, bookingId));

        verify(redisUtil).delete(eq(lockKey1));
        verify(redisUtil).delete(eq(statusKey1));
    }
}


