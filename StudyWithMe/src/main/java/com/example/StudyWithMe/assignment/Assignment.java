package com.example.StudyWithMe.assignment;

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
public class Assignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(columnDefinition = "TEXT")
    private String modelAnswer; // 스터디장 참고용 AI 제안 모범 답안

    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime dueDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_group_id")
    private StudyGroup studyGroup;

    @OneToMany(mappedBy = "assignment", cascade = CascadeType.ALL)
    private List<AssignmentSubmission> submissions = new ArrayList<>();

    public Assignment(String title, String content, String modelAnswer, LocalDateTime dueDate, StudyGroup studyGroup) {
        this.title = title;
        this.content = content;
        this.modelAnswer = modelAnswer;
        this.dueDate = dueDate;
        this.studyGroup = studyGroup;
        this.createdAt = LocalDateTime.now();
    }
}
