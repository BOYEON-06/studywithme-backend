package com.example.StudyWithMe.assignment;

import java.time.LocalDateTime;

public record SubmissionResponseDTO(
        Long submissionId,
        Long memberId,
        String memberName, // 제출자 이름
        String content,    // 제출 내용
        LocalDateTime submittedAt, // 제출 시간
        Integer score,
        String feedback,
        LocalDateTime gradedAt
) {
    public static SubmissionResponseDTO from(AssignmentSubmission submission, String memberName) {
        return new SubmissionResponseDTO(
                submission.getId(),
                submission.getMember().getId(),
                memberName,
                submission.getContent(),
                submission.getSubmittedAt(),
                submission.getScore(),
                submission.getFeedback(),
                submission.getGradedAt()
        );
    }
}
