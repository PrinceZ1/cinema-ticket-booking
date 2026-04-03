package com.princez1.movie_service.dto;

public record MovieResponse(
        Long id,
        String title,
        String description,
        Integer duration,
        String genre,
        String rating) {}
