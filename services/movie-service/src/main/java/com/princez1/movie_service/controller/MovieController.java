package com.princez1.movie_service.controller;

import com.princez1.common_lib.response.ApiResponse;
import com.princez1.movie_service.dto.MovieRequest;
import com.princez1.movie_service.dto.MovieResponse;
import com.princez1.movie_service.dto.ShowtimeRequest;
import com.princez1.movie_service.dto.ShowtimeResponse;
import com.princez1.movie_service.entity.Movie;
import com.princez1.movie_service.service.MovieService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MovieController {

    private final MovieService movieService;

    @GetMapping("/movies")
    public ResponseEntity<ApiResponse<List<MovieResponse>>> getAllMovies() {
        return ResponseEntity.ok(ApiResponse.success(movieService.getAllMovies()));
    }

    @GetMapping("/movies/{id}")
    public ResponseEntity<ApiResponse<MovieResponse>> getMovieById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(movieService.getMovieById(id)));
    }

    @GetMapping("/movies/status/{status}")
    public ResponseEntity<ApiResponse<List<MovieResponse>>> getMoviesByStatus(
            @PathVariable Movie.MovieStatus status) {
        return ResponseEntity.ok(ApiResponse.success(movieService.getMoviesByStatus(status)));
    }

    @PostMapping("/movies")
    public ResponseEntity<ApiResponse<MovieResponse>> createMovie(@Valid @RequestBody MovieRequest movie) {
        MovieResponse created = movieService.createMovie(movie);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(201, "Created", created));
    }

    @PutMapping("/movies/{id}")
    public ResponseEntity<ApiResponse<MovieResponse>> updateMovie(
            @PathVariable Long id, @Valid @RequestBody MovieRequest movie) {
        MovieResponse updated = movieService.updateMovie(id, movie);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    @GetMapping("/movies/{id}/showtimes")
    public ResponseEntity<ApiResponse<List<ShowtimeResponse>>> getShowtimesByMovieId(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(movieService.getShowtimesByMovieId(id)));
    }

    @GetMapping("/showtimes/{id}")
    public ResponseEntity<ApiResponse<ShowtimeResponse>> getShowtime(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(movieService.getShowtimeById(id)));
    }

    @PostMapping("/movies/{movieId}/showtimes")
    public ResponseEntity<ApiResponse<ShowtimeResponse>> createShowtime(
            @PathVariable("movieId") Long movieId, @Valid @RequestBody ShowtimeRequest showtime) {
        ShowtimeResponse created = movieService.createShowtime(movieId, showtime);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(201, "Created", created));
    }
}
