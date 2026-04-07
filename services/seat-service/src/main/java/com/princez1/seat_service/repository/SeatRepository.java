package com.princez1.seat_service.repository;

import com.princez1.seat_service.entity.Seat;
import com.princez1.seat_service.entity.SeatStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {

    List<Seat> findByShowtimeId(Long showtimeId);

    List<Seat> findByShowtimeIdAndStatus(Long showtimeId, SeatStatus status);

    Optional<Seat> findByShowtimeIdAndSeatNumber(Long showtimeId, String seatNumber);

    @Query("SELECT s FROM Seat s WHERE s.status = 'HELD' AND s.heldUntil < :now")
    List<Seat> findExpiredHeldSeats(@Param("now") LocalDateTime now);

    @Modifying
    @Query(
            "UPDATE Seat s SET s.status = :status, s.heldByBookingId = null, s.heldUntil = null WHERE s.id IN :seatIds")
    int bulkUpdateStatus(@Param("seatIds") List<Long> seatIds, @Param("status") SeatStatus status);
}
