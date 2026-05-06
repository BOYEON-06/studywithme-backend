package com.example.StudyWithMe.assignment;

import com.example.StudyWithMe.ai.AiAssignmentResponseDTO;
import com.example.StudyWithMe.ai.AiParameterDTO;
import com.example.StudyWithMe.ai.GeminiService;
import com.example.StudyWithMe.config.PrincipalDetails;
import com.example.StudyWithMe.study.StudyService;
import jakarta.validation.Valid;
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
    private final GeminiService geminiService;

    // AI에게 문제 생성을 요청하는 API (DB 저장 안 함, 단순 제안)
    @PostMapping("/generate-ai")
    public ResponseEntity<?> getAiSuggestion(
            @RequestBody AiParameterDTO request,
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
            // 2. request에서 topic을 꺼내서 전달 (변수명 수정)
            AiAssignmentResponseDTO suggestion = geminiService.generateAssignment(request);
            return ResponseEntity.ok(suggestion);

        } catch (org.springframework.web.client.HttpStatusCodeException e) {
            // 구글 API가 에러를 뱉었을 때 (400, 401, 403, 429 등)
            System.err.println("HTTP 상태 코드: " + e.getStatusCode());
            System.err.println("에러 본문: " + e.getResponseBodyAsString());
            throw new RuntimeException("AI API 호출 실패: " + e.getMessage());
        } catch (Exception e) {
            // 그 외 일반적인 에러 (네트워크, JSON 파싱 등)
            e.printStackTrace(); // 상세한 에러 경로 출력
            throw new RuntimeException("알 수 없는 오류 발생: " + e.getMessage());
        }
    }

    @PostMapping("/{studyId}/confirm-ai")
    public ResponseEntity<?> confirmAiAssignment(
            @PathVariable Long studyId,
            @RequestBody AssignmentRequestDTO dto, // 기존 과제 생성용 DTO를 재사용합니다.
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        if (principalDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "로그인이 필요합니다."));
        }

        try {
            // 기존에 검증 로직이 포함된 서비스 메서드를 그대로 호출하여 DB에 저장합니다.
            Long assignmentId = assignmentService.createAssignment(
                    studyId,
                    dto,
                    principalDetails.getMember().getId()
            );

            return ResponseEntity.ok(Map.of(
                    "message", "AI 제안 과제가 성공적으로 출제되었습니다.",
                    "assignmentId", assignmentId
            ));
        } catch (IllegalStateException e) {
            // 방장 권한이 없는 경우 등
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "과제 저장 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

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

    @GetMapping("/leader")
    public ResponseEntity<?> getLeaderAssignments(
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        if (principalDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            List<LeaderAssignmentResponseDTO> responses =
                    assignmentService.getAssignmentsByLeader(principalDetails.getMember().getId());
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/submissions/{submissionId}/grade")
    public ResponseEntity<?> gradeSubmission(
            @PathVariable Long submissionId,
            @RequestBody @Valid GradeRequestDTO dto,
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        if (principalDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            assignmentService.gradeSubmission(
                    submissionId, dto, principalDetails.getMember().getId()
            );
            return ResponseEntity.ok(Map.of("message", "채점이 완료되었습니다."));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
