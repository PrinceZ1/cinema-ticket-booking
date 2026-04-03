package com.princez1.movie_service.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;

import com.princez1.movie_service.dto.MovieRequest;
import com.princez1.movie_service.dto.MovieResponse;
import com.princez1.movie_service.exception.GlobalExceptionHandler;
import com.princez1.movie_service.exception.ResourceNotFoundException;
import com.princez1.movie_service.service.MovieService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(MovieController.class)
@Import(GlobalExceptionHandler.class)
class MovieControllerTest {

    @Autowired private MockMvc mockMvc;

    @Autowired private ObjectMapper objectMapper;

    @MockBean private MovieService movieService;

    @Test
    void testGetAllMovies_returnsOk() throws Exception {
        when(movieService.getAllMovies()).thenReturn(List.of());

        mockMvc
                .perform(get("/api/movies").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void testGetMovieById_notFound_returns404() throws Exception {
        when(movieService.getMovieById(1L))
                .thenThrow(new ResourceNotFoundException("Movie", "1"));

        mockMvc
                .perform(get("/api/movies/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreateMovie_validInput_returns201() throws Exception {
        MovieRequest request =
                new MovieRequest("Inception", "Dreams within dreams", 120, "Sci-Fi", null);

        MovieResponse created =
                new MovieResponse(1L, request.getTitle(), request.getDescription(), request.getDuration(), request.getGenre(), null);

        when(movieService.createMovie(any(MovieRequest.class))).thenReturn(created);

        mockMvc
                .perform(
                        post("/api/movies")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.data.id").value(1));
    }
}


