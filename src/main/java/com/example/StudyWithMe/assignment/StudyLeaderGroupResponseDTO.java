package com.example.StudyWithMe.assignment;

import java.util.List;

// 스터디별로 과제 묶음을 관리
public record StudyLeaderGroupResponseDTO(
        Long studyGroupId,
        String studyTitle,
        List<AssignmentGroupResponseDTO> assignmentGroups
) {}
