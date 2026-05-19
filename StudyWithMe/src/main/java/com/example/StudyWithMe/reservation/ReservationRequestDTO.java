package com.example.StudyWithMe.reservation;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class ReservationRequestDTO {
    private String title;
    private String content;
    private String modelAnswer;
    private LocalDateTime openAt;  // 예약 시작 시간
    private LocalDateTime closeAt; // 제한 시간(마감)
}
