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
        boolean isSubmitted
) {
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
                isSubmitted
        );
    }
}
