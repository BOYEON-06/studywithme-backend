package com.example.StudyWithMe.assignment;

import java.util.List;

// 특정 과제와 그 과제에 달린 제출물 리스트를 묶음
public record AssignmentGroupResponseDTO(
        AssignmentResponseDTO assignment,
        String modelAnswer,
        List<SubmissionResponseDTO> submissions
) {}