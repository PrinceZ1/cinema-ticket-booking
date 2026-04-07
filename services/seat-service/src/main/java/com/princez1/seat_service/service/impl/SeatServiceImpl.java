package com.princez1.seat_service.service.impl;

import com.princez1.common_lib.redis.RedisUtil;
import com.princez1.seat_service.dto.SeatResponse;
import com.princez1.seat_service.entity.Seat;
import com.princez1.seat_service.entity.SeatStatus;
import com.princez1.seat_service.exception.InvalidSeatOperationException;
import com.princez1.seat_service.exception.ResourceNotFoundException;
import com.princez1.seat_service.exception.SeatNotAvailableException;
import com.princez1.seat_service.repository.SeatRepository;
import com.princez1.seat_service.service.SeatService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SeatServiceImpl implements SeatService {

    private static final String SEAT_LOCK_KEY = "seat:lock:%d";
    private static final String SEAT_STATUS_KEY = "seat:status:%d";
    private static final long LOCK_TTL_MINUTES = 10L;

    private final SeatRepository seatRepository;
    private final RedisUtil redisUtil;

    @Override
    public List<SeatResponse> getSeatsByShowtime(Long showtimeId) {
        log.debug("getSeatsByShowtime(showtimeId={})", showtimeId);
        return seatRepository.findByShowtimeId(showtimeId).stream().map(this::toResponse).toList();
    }

    @Override
    public List<SeatResponse> getAvailableSeatsByShowtime(Long showtimeId) {
        log.debug("getAvailableSeatsByShowtime(showtimeId={})", showtimeId);
        return seatRepository.findByShowtimeIdAndStatus(showtimeId, SeatStatus.AVAILABLE).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public SeatResponse getSeatById(Long id) {
        log.debug("getSeatById(id={})", id);
        return toResponse(
                seatRepository
                        .findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Seat", String.valueOf(id))));
    }

    @Override
    public boolean isSeatAvailable(Long seatId) {
        log.debug("isSeatAvailable(seatId={})", seatId);
        return seatRepository.findById(seatId).map(s -> s.getStatus() == SeatStatus.AVAILABLE).orElse(false);
    }

    @Override
    public SeatResponse holdSeat(Long seatId, String bookingId) {
        log.info("holdSeat(seatId={}, bookingId={})", seatId, bookingId);

        String lockKey = String.format(SEAT_LOCK_KEY, seatId);
        boolean locked = redisUtil.setIfAbsent(lockKey, bookingId, LOCK_TTL_MINUTES, TimeUnit.MINUTES);
        if (!locked) {
            log.warn("Seat {} is already locked by another booking", seatId);
            throw new SeatNotAvailableException("Seat " + seatId + " is currently held");
        }

        Seat seat =
                seatRepository
                        .findById(seatId)
                        .orElseThrow(() -> new ResourceNotFoundException("Seat", String.valueOf(seatId)));

        if (seat.getStatus() != SeatStatus.AVAILABLE) {
            redisUtil.delete(lockKey); // release lock we just acquired
            throw new SeatNotAvailableException("Seat " + seatId + " is not available");
        }

        seat.setStatus(SeatStatus.HELD);
        seat.setHeldByBookingId(bookingId);
        seat.setHeldUntil(LocalDateTime.now().plusMinutes(LOCK_TTL_MINUTES));

        Seat saved = seatRepository.save(seat);

        redisUtil.set(
                String.format(SEAT_STATUS_KEY, seatId), "HELD", LOCK_TTL_MINUTES, TimeUnit.MINUTES);

        log.info("Seat {} held successfully for booking {}", seatId, bookingId);
        return toResponse(saved);
    }

    @Override
    public SeatResponse confirmSeat(Long seatId, String bookingId) {
        log.info("confirmSeat(seatId={}, bookingId={})", seatId, bookingId);

        Seat seat =
                seatRepository
                        .findById(seatId)
                        .orElseThrow(() -> new ResourceNotFoundException("Seat", String.valueOf(seatId)));

        if (seat.getStatus() != SeatStatus.HELD
                || seat.getHeldByBookingId() == null
                || !seat.getHeldByBookingId().equals(bookingId)) {
            throw new InvalidSeatOperationException(
                    "Seat " + seatId + " cannot be confirmed for booking " + bookingId);
        }

        seat.setStatus(SeatStatus.BOOKED);
        seat.setHeldUntil(null);

        Seat saved = seatRepository.save(seat);

        redisUtil.set(String.format(SEAT_STATUS_KEY, seatId), "BOOKED", 24L, TimeUnit.HOURS);
        redisUtil.delete(String.format(SEAT_LOCK_KEY, seatId));

        log.info("Seat {} confirmed as BOOKED for booking {}", seatId, bookingId);
        return toResponse(saved);
    }

    @Override
    public SeatResponse releaseSeat(Long seatId, String bookingId) {
        log.info("releaseSeat(seatId={}, bookingId={})", seatId, bookingId);

        Seat seat =
                seatRepository
                        .findById(seatId)
                        .orElseThrow(() -> new ResourceNotFoundException("Seat", String.valueOf(seatId)));

        String heldByBookingId = seat.getHeldByBookingId();
        if (heldByBookingId == null || !heldByBookingId.equals(bookingId)) {
            log.warn(
                    "Seat {} release rejected. heldByBookingId={}, requested={}",
                    seatId,
                    heldByBookingId,
                    bookingId);
            throw new InvalidSeatOperationException(
                    "Seat " + seatId + " cannot be released for booking " + bookingId);
        }

        seat.setStatus(SeatStatus.AVAILABLE);
        seat.setHeldByBookingId(null);
        seat.setHeldUntil(null);

        Seat saved = seatRepository.save(seat);

        redisUtil.delete(String.format(SEAT_LOCK_KEY, seatId));
        redisUtil.delete(String.format(SEAT_STATUS_KEY, seatId));

        log.info("Seat {} released back to AVAILABLE for booking {}", seatId, bookingId);
        return toResponse(saved);
    }

    @Override
    public List<SeatResponse> holdMultipleSeats(List<Long> seatIds, String bookingId) {
        log.info("holdMultipleSeats(seats={}, bookingId={})", seatIds.size(), bookingId);

        List<Long> successfullyHeld = new ArrayList<>();
        try {
            for (Long seatId : seatIds) {
                holdSeat(seatId, bookingId);
                successfullyHeld.add(seatId);
            }
        } catch (SeatNotAvailableException e) {
            log.warn("Failed to hold seat, rolling back {} seats for booking {}", successfullyHeld.size(), bookingId);
            successfullyHeld.forEach(
                    id -> {
                        try {
                            releaseSeat(id, bookingId);
                        } catch (Exception ex) {
                            log.error("Rollback failed for seat {}", id, ex);
                        }
                    });
            throw e;
        }

        return seatRepository.findAllById(successfullyHeld).stream().map(this::toResponse).toList();
    }

    @Override
    public void releaseMultipleSeats(List<Long> seatIds, String bookingId) {
        log.info("releaseMultipleSeats(seats={}, bookingId={})", seatIds.size(), bookingId);
        seatIds.forEach(id -> releaseSeat(id, bookingId));
    }

    @Override
    public void confirmMultipleSeats(List<Long> seatIds, String bookingId) {
        log.info("confirmMultipleSeats(seats={}, bookingId={})", seatIds.size(), bookingId);
        seatIds.forEach(id -> confirmSeat(id, bookingId));
    }

    @Override
    @Scheduled(fixedRate = 60000)
    public void releaseExpiredHeldSeats() {
        log.debug("releaseExpiredHeldSeats()");

        List<Seat> expired = seatRepository.findExpiredHeldSeats(LocalDateTime.now());
        expired.forEach(
                seat -> {
                    seat.setStatus(SeatStatus.AVAILABLE);
                    seat.setHeldByBookingId(null);
                    seat.setHeldUntil(null);
                    redisUtil.delete(String.format(SEAT_LOCK_KEY, seat.getId()));
                    redisUtil.delete(String.format(SEAT_STATUS_KEY, seat.getId()));
                });
        seatRepository.saveAll(expired);
        if (!expired.isEmpty()) {
            log.info("Released {} expired held seats", expired.size());
        }
    }

    @Override
    public void initializeSeatsForShowtime(Long showtimeId, int rows, int seatsPerRow) {
        log.info("initializeSeatsForShowtime(showtimeId={}, rows={}, seatsPerRow={})", showtimeId, rows, seatsPerRow);

        if (rows <= 0 || seatsPerRow <= 0) {
            throw new IllegalArgumentException("rows and seatsPerRow must be positive");
        }

        List<Seat> existing = seatRepository.findByShowtimeId(showtimeId);
        if (!existing.isEmpty()) {
            log.info("Seats already exist for showtime {}", showtimeId);
            return;
        }

        int total = rows * seatsPerRow;
        List<Seat> generatedSeats = new ArrayList<>(total);

        int rowLimit = Math.min(rows, 26); // A-Z
        for (int r = 0; r < rowLimit; r++) {
            String rowLetter = String.valueOf((char) ('A' + r));
            for (int seatIndex = 1; seatIndex <= seatsPerRow; seatIndex++) {
                String seatNumber = rowLetter + seatIndex;
                Seat seat =
                        Seat.builder()
                                .showtimeId(showtimeId)
                                .seatNumber(seatNumber)
                                .row(rowLetter)
                                .seatIndex(seatIndex)
                                .status(SeatStatus.AVAILABLE)
                                .build();
                generatedSeats.add(seat);
            }
        }

        seatRepository.saveAll(generatedSeats);
        log.info("Initialized {} seats for showtime {}", generatedSeats.size(), showtimeId);
    }

    private SeatResponse toResponse(Seat seat) {
        return new SeatResponse(
                seat.getId(),
                seat.getShowtimeId(),
                seat.getSeatNumber(),
                seat.getRow(),
                seat.getStatus() != null ? seat.getStatus().name() : null);
    }
}

