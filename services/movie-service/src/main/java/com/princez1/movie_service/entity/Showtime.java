package com.princez1.movie_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "showtimes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Showtime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;

    @NotNull
    @Column(name = "movie_id", nullable = false, insertable = false, updatable = false)
    private Long movieId;

    @NotNull
    @Column(nullable = false)
    private LocalDateTime startTime;

    @NotNull
    @Column(nullable = false)
    private LocalDateTime endTime;

    @NotBlank
    @Column(nullable = false)
    private String hall; // e.g. "Hall A", "Hall B"

    @NotNull
    @Column(nullable = false)
    private Integer totalSeats;

    @NotNull
    @Column(nullable = false)
    private Integer availableSeats;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal ticketPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ShowtimeStatus status;

    public enum ShowtimeStatus {
        SCHEDULED,
        ONGOING,
        COMPLETED,
        CANCELLED
    }
}
