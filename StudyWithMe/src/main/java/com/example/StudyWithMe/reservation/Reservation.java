package com.example.StudyWithMe.reservation;

import com.example.StudyWithMe.study.StudyGroup;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(columnDefinition = "TEXT")
    private String modelAnswer;

    private LocalDateTime createdAt;
    private LocalDateTime openAt;  // 예약 오픈 시간
    private LocalDateTime closeAt; // 제한 마감 시간

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_group_id")
    private StudyGroup studyGroup;

    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL)
    private List<ReservationSubmission> submissions = new ArrayList<>();

    public Reservation(String title, String content, String modelAnswer, LocalDateTime openAt, LocalDateTime closeAt, StudyGroup studyGroup) {
        this.title = title;
        this.content = content;
        this.modelAnswer = modelAnswer;
        this.openAt = openAt;
        this.closeAt = closeAt;
        this.studyGroup = studyGroup;
        this.createdAt = LocalDateTime.now();
    }

    // 시간 검증 비즈니스 로직
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