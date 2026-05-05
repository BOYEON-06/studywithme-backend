package com.example.StudyWithMe.assignment;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

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

    @Column(nullable = false)
    private Long memberId; // 누가 제출했는지 (Member 엔티티와 연관관계 맺어도 됨)

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content; // 서술형 답안 내용

    private LocalDateTime submittedAt;

    public AssignmentSubmission(Assignment assignment, Long memberId, String content) {
        this.assignment = assignment;
        this.memberId = memberId;
        this.content = content;
        this.submittedAt = LocalDateTime.now();
    }
}