package com.example.StudyWithMe.reservation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

// Redis에 저장하고 꺼내올 캐시 전용 DTO (Serializable 필수)
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReservationCacheDTO implements Serializable {
    private Long id;
    private String title;
    private String content;
    private LocalDateTime openAt;
    private LocalDateTime closeAt;

    public static ReservationCacheDTO from(Reservation reservation) {
        return new ReservationCacheDTO(
                reservation.getId(),
                reservation.getTitle(),
                reservation.getContent(),
                reservation.getOpenAt(),
                reservation.getCloseAt()
        );
    }

    // 캐시 데이터를 기반으로 시간 검증 진행
    public void validateAccessible() {
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(this.openAt)) {
            throw new IllegalArgumentException("아직 과제 제한 시간(오픈 전)이 아닙니다.");
        }
        if (now.isAfter(this.closeAt)) {
            throw new IllegalArgumentException("마감 기한이 지나 더 이상 접근할 수 없습니다.");
        }
    }
}
