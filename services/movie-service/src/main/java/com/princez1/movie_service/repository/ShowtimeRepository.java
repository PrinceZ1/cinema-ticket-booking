package com.princez1.movie_service.repository;

import com.princez1.movie_service.entity.Showtime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ShowtimeRepository extends JpaRepository<Showtime, Long> {

    List<Showtime> findByMovieId(Long movieId);

    List<Showtime> findByMovieIdAndStatus(Long movieId, Showtime.ShowtimeStatus status);

    List<Showtime> findByStartTimeBetween(LocalDateTime from, LocalDateTime to);
}
