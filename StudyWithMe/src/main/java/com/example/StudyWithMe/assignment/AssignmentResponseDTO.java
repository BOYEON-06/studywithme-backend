package com.example.StudyWithMe.assignment;

import java.time.LocalDateTime;
public record AssignmentResponseDTO(
        Long studyId,
        Long assignmentId,
        String creatorName,
        String studyTitle,
        String title,
        String content,
        LocalDateTime dueDate,
        boolean isExpired,
        boolean isSubmitted,
        // 새로 추가한 필드들
        String submittedContent,
        LocalDateTime submittedAt,
        Integer score,
        String feedback,
        LocalDateTime gradedAt
) {
    // 1. 기존처럼 제출 여부만 알 때 (과제 목록 조회 등)
    public static AssignmentResponseDTO of(Assignment assignment, boolean isSubmitted) {
        return new AssignmentResponseDTO(
                assignment.getStudyGroup().getId(),
                assignment.getId(),
                assignment.getStudyGroup().getCreator().getName(),
                assignment.getStudyGroup().getTitle(),
                assignment.getTitle(),
                assignment.getContent(),
                assignment.getDueDate(),
                assignment.getDueDate().isBefore(LocalDateTime.now()),
                isSubmitted,
                null, null, null, null, null // 상세 정보는 없으므로 null
        );
    }

    // 2. 제출물 정보가 있을 때 (채점 화면, 과제 상세 조회 등)
    public static AssignmentResponseDTO of(Assignment assignment, AssignmentSubmission submission) {
        return new AssignmentResponseDTO(
                assignment.getStudyGroup().getId(),
                assignment.getId(),
                assignment.getStudyGroup().getCreator().getName(),
                assignment.getStudyGroup().getTitle(),
                assignment.getTitle(),
                assignment.getContent(),
                assignment.getDueDate(),
                assignment.getDueDate().isBefore(LocalDateTime.now()),
                submission != null,
                submission != null ? submission.getContent() : null,
                submission != null ? submission.getSubmittedAt() : null,
                submission != null ? submission.getScore() : null,
                submission != null ? submission.getFeedback() : null,
                submission != null ? submission.getGradedAt() : null
        );
    }
}