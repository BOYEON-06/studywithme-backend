package com.example.StudyWithMe.reservation;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ReservationResponseDTO {
    private Long id;
    private String title;
    private String content;
    private LocalDateTime openAt;
    private LocalDateTime closeAt;

    public static ReservationResponseDTO of(Reservation reservation) {
        return new ReservationResponseDTO(
                reservation.getId(),
                reservation.getTitle(),
                reservation.getContent(),
                reservation.getOpenAt(),
                reservation.getCloseAt()
        );
    }
}
