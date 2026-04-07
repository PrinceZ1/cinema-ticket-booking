package com.princez1.seat_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "seats")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FK reference to movie-service
    @Column(nullable = false)
    private Long showtimeId;

    @Column(nullable = false)
    private String seatNumber; // e.g. "A1", "B12"

    @Column(nullable = false)
    private String row; // e.g. "A", "B", "C"

    @Column(nullable = false)
    private Integer seatIndex; // numeric position within row

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeatStatus status; // AVAILABLE, HELD, BOOKED

    // nullable — which booking currently holds this seat
    private String heldByBookingId;

    // nullable — when the HELD status expires
    private LocalDateTime heldUntil;

    // Optimistic locking: second line of defense
    @Version
    private Long version;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
