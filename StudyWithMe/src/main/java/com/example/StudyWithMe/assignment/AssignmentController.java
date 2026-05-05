package com.example.StudyWithMe.assignment;

import com.example.StudyWithMe.config.PrincipalDetails;
import com.example.StudyWithMe.study.StudyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.*;

import java.time.DateTimeException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/assignments")
@RequiredArgsConstructor
public class AssignmentController {
    private final AssignmentService assignmentService;

    @PostMapping("/{studyId}")
    public ResponseEntity<?> createAssignment(
            @PathVariable Long studyId,
            @RequestBody AssignmentRequestDTO request,
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        if (principalDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("code", "LOGIN_REQUIRED", "message", "로그인이 필요합니다."));
        }

        try {
            Long assignmentId = assignmentService.createAssignment(studyId, request, principalDetails.getMember().getId());
            return ResponseEntity.ok(Map.of(
                    "message", "과제가 성공적으로 등록되었습니다.",
                    "assignmentId", assignmentId
            ));
        } catch (IllegalStateException e) {
            // 1. 권한 부족 (방장이 아님)
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of(
                            "code", "NOT_LEADER",
                            "message", e.getMessage()
                    ));
        } catch (IllegalArgumentException e) {
            // 2. 서비스에서 던진 구체적인 입력 값 에러 (스터디 없음, 마감 기한 과거 등)
            String errorCode = "INVALID_INPUT";
            if (e.getMessage().contains("마감 기한")) errorCode = "INVALID_DUE_DATE";
            if (e.getMessage().contains("스터디")) errorCode = "STUDY_NOT_FOUND";

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "code", errorCode,
                            "message", e.getMessage() // 서비스의 구체적인 메시지 전달
                    ));
        } catch (Exception e) {
            // 3. 기타 예상치 못한 서버 에러
            return ResponseEntity.internalServerError()
                    .body(Map.of(
                            "code", "SERVER_ERROR",
                            "message", "서버 오류가 발생했습니다."
                    ));
        }
    }

    @PostMapping("/{studyId}/submit/{assignmentId}")
    public ResponseEntity<?> submitAssignment(
            @PathVariable Long studyId,
            @PathVariable Long assignmentId,
            @RequestBody AssignmentSubmitDTO request,
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        if (principalDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "code", "LOGIN_REQUIRED",
                            "message", "로그인이 필요합니다."
                    ));
        }

        try {
            Long submissionId = assignmentService.submitAssignment(
                    studyId,
                    assignmentId,
                    principalDetails.getMember().getId(),
                    request.content()
            );

            return ResponseEntity.ok(Map.of(
                    "message", "과제가 성공적으로 제출되었습니다.",
                    "submissionId", submissionId
            ));
        } catch (IllegalArgumentException e) {
            // 1. 존재하지 않는 과제거나 정보 불일치 (400)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("code", "INVALID_REQUEST", "message", e.getMessage()));

        } catch (IllegalStateException e) {
            // 2. 비즈니스 로직 거부 (스터디원 아님, 기한 지남, 중복 제출 등)
            String errorCode = "SUBMISSION_REJECTED";

            // 메시지에 따른 세부 코드 분기 (프론트 배려)
            if (e.getMessage().contains("마감")) errorCode = "PAST_DEADLINE";
            if (e.getMessage().contains("이미 제출")) errorCode = "ALREADY_SUBMITTED";
            if (e.getMessage().contains("멤버가 아닙니다")) errorCode = "NOT_A_MEMBER";

            return ResponseEntity.status(HttpStatus.FORBIDDEN) // 권한/상태 문제는 403 권장
                    .body(Map.of("code", errorCode, "message", e.getMessage()));

        } catch (Exception e) {
            // 3. 기타 서버 에러
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("code", "SERVER_ERROR", "message", "제출 중 오류가 발생했습니다."));
        }
    }

    @GetMapping("/my-assignments")
    public ResponseEntity<List<AssignmentResponseDTO>> getMyAssignments(
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        if (principalDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<AssignmentResponseDTO> assignments =
                assignmentService.getMyAssignments(principalDetails.getMember().getId());

        return ResponseEntity.ok(assignments);
    }

    @GetMapping("/{studyId}/submissions/{assignmentId}")
    public ResponseEntity<?> getSubmissions(
            @PathVariable Long studyId,
            @PathVariable Long assignmentId,
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        if (principalDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            List<SubmissionResponseDTO> responses = assignmentService.getSubmissionsByLeader(
                    studyId, assignmentId, principalDetails.getMember().getId()
            );
            return ResponseEntity.ok(responses);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("code", "NOT_LEADER", "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
