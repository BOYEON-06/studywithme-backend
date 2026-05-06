package com.example.StudyWithMe.assignment;

import com.example.StudyWithMe.study.StudyGroup;

import java.time.LocalDateTime;

public record LeaderAssignmentResponseDTO(
        Long submissionId,    // 제출물 식별 (채점용)
        Long assignmentId,    // 과제 식별
        Long studyGroupId,    // 스터디 식별
        String studyTitle,
        String studentName,
        String taskTitle,
        String submitContent,
        Integer score,
        int submissionCount
) {
    public static LeaderAssignmentResponseDTO from(AssignmentSubmission submission) {
        Assignment assignment = submission.getAssignment();
        StudyGroup studyGroup = assignment.getStudyGroup();

        return new LeaderAssignmentResponseDTO(
                submission.getId(),
                assignment.getId(),
                studyGroup.getId(),
                studyGroup.getTitle(),
                submission.getMember().getName(),
                assignment.getTitle(),
                submission.getContent(),
                submission.getScore(),
                // 과제(Assignment) 엔티티가 가진 제출물 리스트의 크기를 측정
                assignment.getSubmissions().size()
        );
    }
}
