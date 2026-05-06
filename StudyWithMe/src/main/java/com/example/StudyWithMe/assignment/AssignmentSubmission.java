package com.example.StudyWithMe.assignment;

import com.example.StudyWithMe.member.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AssignmentSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id")
    private Assignment assignment; // 어떤 과제에 대한 제출인지

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

    public AssignmentSubmission(Assignment assignment, Member member, String content) {
        this.assignment = assignment;
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