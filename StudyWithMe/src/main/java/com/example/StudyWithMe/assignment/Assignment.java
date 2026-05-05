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

    private LocalDateTime createdAt;

    private LocalDateTime dueDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_group_id")
    private StudyGroup studyGroup;

    @OneToMany(mappedBy = "assignment", cascade = CascadeType.ALL)
    private List<AssignmentSubmission> submissions = new ArrayList<>();

    public Assignment(String title, String content, LocalDateTime dueDate, StudyGroup studyGroup) {
        this.title = title;
        this.content = content;
        this.dueDate = dueDate;
        this.studyGroup = studyGroup;
        this.createdAt = LocalDateTime.now();
    }
}
