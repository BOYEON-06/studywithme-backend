package com.example.StudyWithMe.ai;

import java.util.List;

// 1. 프론트엔드와 주고받을 DTO
public record AiAssignmentResponseDTO(
        String title,
        String content,
        String modelAnswer // AI 제안 모범 답안
) {}

