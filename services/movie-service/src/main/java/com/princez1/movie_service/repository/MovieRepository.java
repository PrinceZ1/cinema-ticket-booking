package com.princez1.movie_service.repository;

import com.princez1.movie_service.entity.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {

    Optional<Movie> findByTitle(String title);

    List<Movie> findByStatus(Movie.MovieStatus status);

    List<Movie> findByGenre(String genre);
}
