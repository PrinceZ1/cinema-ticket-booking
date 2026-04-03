package com.princez1.movie_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShowtimeRequest {
    private Long movieId;
    private String startTime;
    private String endTime;
    private String cinemaHall;
    private Integer availableSeats;
}
