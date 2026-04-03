package com.princez1.movie_service.dto;

public record ShowtimeResponse(
        Long id,
        Long movieId,
        String startTime,
        String endTime,
        String cinemaHall,
        Integer availableSeats) {}
