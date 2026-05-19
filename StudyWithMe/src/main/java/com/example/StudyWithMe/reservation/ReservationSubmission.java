package com.example.StudyWithMe.reservation;

import com.example.StudyWithMe.assignment.Assignment;
import com.example.StudyWithMe.member.Member;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
public class ReservationSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id")
    private Reservation reservation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member; // 누가 제출했는지

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content; // 서술형 답안 내용

    private LocalDateTime submittedAt;

    private Integer score; // 점수 (채점 전에는 null)

    @Column(columnDefinition = "TEXT")
    private String feedback; // 방장의 피드백

    private LocalDateTime gradedAt; // 채점 일시

    public ReservationSubmission(Reservation reservation, Member member, String content) {
        this.reservation = reservation;
        this.member = member;
        this.content = content;
        this.submittedAt = LocalDateTime.now();
    }

    // 채점을 수행하는 메서드
    public void updateGrade(Integer score, String feedback) {
        this.score = score;
        this.feedback = feedback;
        this.gradedAt = LocalDateTime.now();
    }
}
