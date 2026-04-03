package com.princez1.movie_service.service.impl;

import com.princez1.movie_service.dto.MovieRequest;
import com.princez1.movie_service.dto.MovieResponse;
import com.princez1.movie_service.dto.ShowtimeRequest;
import com.princez1.movie_service.dto.ShowtimeResponse;
import com.princez1.movie_service.entity.Movie;
import com.princez1.movie_service.entity.Showtime;
import com.princez1.movie_service.exception.ResourceNotFoundException;
import com.princez1.movie_service.repository.MovieRepository;
import com.princez1.movie_service.repository.ShowtimeRepository;
import com.princez1.movie_service.service.MovieService;
import jakarta.transaction.Transactional;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MovieServiceImpl implements MovieService {

    private final MovieRepository movieRepository;
    private final ShowtimeRepository showtimeRepository;

    @Override
    @Transactional
    public List<MovieResponse> getAllMovies() {
        log.debug("getAllMovies()");
        return movieRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public MovieResponse getMovieById(Long id) {
        log.debug("getMovieById(id={})", id);
        return toResponse(getMovieEntityById(id));
    }

    @Override
    @Transactional
    public List<MovieResponse> getMoviesByStatus(Movie.MovieStatus status) {
        log.debug("getMoviesByStatus(status={})", status);
        return movieRepository.findByStatus(status).stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public MovieResponse createMovie(MovieRequest movie) {
        log.debug("createMovie()");
        Movie saved = movieRepository.save(toEntity(movie));
        return toResponse(saved);
    }

    @Override
    @Transactional
    public MovieResponse updateMovie(Long id, MovieRequest movie) {
        log.debug("updateMovie(id={})", id);
        Movie existing = getMovieEntityById(id);

        Movie updated = applyMovieUpdate(existing, movie);
        return toResponse(movieRepository.save(updated));
    }

    @Override
    @Transactional
    public List<ShowtimeResponse> getShowtimesByMovieId(Long movieId) {
        log.debug("getShowtimesByMovieId(movieId={})", movieId);
        return showtimeRepository.findByMovieId(movieId).stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public ShowtimeResponse getShowtimeById(Long id) {
        log.debug("getShowtimeById(id={})", id);
        return toResponse(
                showtimeRepository
                        .findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Showtime", String.valueOf(id))));
    }

    @Override
    @Transactional
    public ShowtimeResponse createShowtime(Long movieId, ShowtimeRequest showtime) {
        log.debug("createShowtime(movieId={})", movieId);

        Showtime entity = toEntity(showtime);
        if (entity.getStartTime() == null || entity.getEndTime() == null) {
            throw new ValidationException("startTime and endTime are required");
        }
        if (!entity.getStartTime().isBefore(entity.getEndTime())) {
            throw new ValidationException("startTime must be before endTime");
        }

        Movie movie = getMovieEntityById(movieId);
        entity.setMovie(movie);
        entity.setMovieId(movieId);
        entity.setAvailableSeats(entity.getTotalSeats());
        if (entity.getStatus() == null) {
            entity.setStatus(Showtime.ShowtimeStatus.SCHEDULED);
        }

        return toResponse(showtimeRepository.save(entity));
    }

    private Movie getMovieEntityById(Long id) {
        return movieRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie", String.valueOf(id)));
    }

    private MovieResponse toResponse(Movie movie) {
        return new MovieResponse(
                movie.getId(),
                movie.getTitle(),
                movie.getDescription(),
                movie.getDuration(),
                movie.getGenre(),
                null);
    }

    private Movie toEntity(MovieRequest request) {
        Movie movie = new Movie();
        movie.setTitle(request.getTitle());
        movie.setDescription(request.getDescription());
        movie.setDuration(request.getDuration());
        movie.setGenre(request.getGenre());
        return movie;
    }

    private Movie applyMovieUpdate(Movie existing, MovieRequest request) {
        Movie updated =
                Movie.builder()
                        .id(existing.getId())
                        .title(request.getTitle())
                        .description(request.getDescription())
                        .duration(request.getDuration())
                        .genre(request.getGenre())
                        .releaseDate(existing.getReleaseDate())
                        .status(existing.getStatus())
                        .build();
        return updated;
    }

    private ShowtimeResponse toResponse(Showtime showtime) {
        return new ShowtimeResponse(
                showtime.getId(),
                showtime.getMovieId(),
                showtime.getStartTime() != null ? showtime.getStartTime().toString() : null,
                showtime.getEndTime() != null ? showtime.getEndTime().toString() : null,
                showtime.getHall(),
                showtime.getAvailableSeats());
    }

    private Showtime toEntity(ShowtimeRequest request) {
        Showtime showtime = new Showtime();
        showtime.setMovieId(request.getMovieId());
        showtime.setStartTime(parseDateTimeOrNull(request.getStartTime()));
        showtime.setEndTime(parseDateTimeOrNull(request.getEndTime()));
        showtime.setHall(request.getCinemaHall());
        // request doesn't carry totalSeats; reuse availableSeats to populate totalSeats
        if (request.getAvailableSeats() != null) {
            showtime.setTotalSeats(request.getAvailableSeats());
            showtime.setAvailableSeats(request.getAvailableSeats());
        }
        return showtime;
    }

    private LocalDateTime parseDateTimeOrNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return LocalDateTime.parse(value.trim());
    }
}
