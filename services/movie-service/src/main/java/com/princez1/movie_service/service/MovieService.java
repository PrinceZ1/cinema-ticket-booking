package com.princez1.movie_service.service;

import com.princez1.movie_service.dto.MovieRequest;
import com.princez1.movie_service.dto.MovieResponse;
import com.princez1.movie_service.dto.ShowtimeRequest;
import com.princez1.movie_service.dto.ShowtimeResponse;
import com.princez1.movie_service.entity.Movie;

import java.util.List;

public interface MovieService {

    List<MovieResponse> getAllMovies();

    MovieResponse getMovieById(Long id);

    List<MovieResponse> getMoviesByStatus(Movie.MovieStatus status);

    MovieResponse createMovie(MovieRequest movie);

    MovieResponse updateMovie(Long id, MovieRequest movie);

    List<ShowtimeResponse> getShowtimesByMovieId(Long movieId);

    ShowtimeResponse getShowtimeById(Long id);

    ShowtimeResponse createShowtime(Long movieId, ShowtimeRequest showtime);
}

