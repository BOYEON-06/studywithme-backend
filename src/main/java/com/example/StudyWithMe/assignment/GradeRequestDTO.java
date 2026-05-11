package com.example.StudyWithMe.assignment;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record GradeRequestDTO(
        @Min(0) @Max(100) Integer score, // 0~100점 사이 제한 (선택)
        String feedback
) {}
