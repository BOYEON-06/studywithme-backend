package com.example.StudyWithMe.assignment;

import com.example.StudyWithMe.ai.AiAssignmentResponseDTO;
import com.example.StudyWithMe.ai.AiParameterDTO;
import com.example.StudyWithMe.ai.GeminiService;
import com.example.StudyWithMe.config.SessionUtil;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
            HttpSession session // 💡 변경: HttpSession 주입
    ) {
        Long userId = SessionUtil.getLoginUserId(session); // 💡 변경: 세션 유틸 적용

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "code", "LOGIN_REQUIRED",
                            "message", "로그인이 필요합니다."
                    ));
        }

        try {
            AiAssignmentResponseDTO suggestion = geminiService.generateAssignment(request);
            return ResponseEntity.ok(suggestion);

        } catch (org.springframework.web.client.HttpStatusCodeException e) {
            System.err.println("HTTP 상태 코드: " + e.getStatusCode());
            System.err.println("에러 본문: " + e.getResponseBodyAsString());
            throw new RuntimeException("AI API 호출 실패: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("알 수 없는 오류 발생: " + e.getMessage());
        }
    }

    @PostMapping("/{studyId}/confirm-ai")
    public ResponseEntity<?> confirmAiAssignment(
            @PathVariable Long studyId,
            @RequestBody AssignmentRequestDTO dto,
            HttpSession session // 💡 변경: HttpSession 주입
    ) {
        System.out.println("세션 ID: " + session.getId());
        System.out.println("세션 내 유저 ID: " + session.getAttribute("YOUR_SESSION_KEY"));

        Long userId = SessionUtil.getLoginUserId(session);

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "로그인이 필요합니다."));
        }

        try {
            Long assignmentId = assignmentService.createAssignment(
                    studyId,
                    dto,
                    userId // 💡 변경: 추출한 userId 전달
            );

            return ResponseEntity.ok(Map.of(
                    "message", "AI 제안 과제가 성공적으로 출제되었습니다.",
                    "assignmentId", assignmentId
            ));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of(
                            "code", "NOT_LEADER",
                            "message", e.getMessage()
                    ));
        } catch (IllegalArgumentException e) {
            String errorCode = "INVALID_INPUT";
            if (e.getMessage().contains("마감 기한")) errorCode = "INVALID_DUE_DATE";
            if (e.getMessage().contains("스터디")) errorCode = "STUDY_NOT_FOUND";

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "code", errorCode,
                            "message", e.getMessage()
                    ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of(
                            "code", "SERVER_ERROR",
                            "message", "서버 오류가 발생했습니다."
                    ));
        }
    }

    @PostMapping("/{studyId}")
    public ResponseEntity<?> createAssignment(
            @PathVariable Long studyId,
            @RequestBody AssignmentRequestDTO request,
            HttpSession session // 💡 변경: HttpSession 주입
    ) {
        Long userId = SessionUtil.getLoginUserId(session); // 💡 변경: 세션 유틸 적용

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("code", "LOGIN_REQUIRED", "message", "로그인이 필요합니다."));
        }

        try {
            Long assignmentId = assignmentService.createAssignment(studyId, request, userId); // 💡 변경
            return ResponseEntity.ok(Map.of(
                    "message", "과제가 성공적으로 등록되었습니다.",
                    "assignmentId", assignmentId
            ));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of(
                            "code", "NOT_LEADER",
                            "message", e.getMessage()
                    ));
        } catch (IllegalArgumentException e) {
            String errorCode = "INVALID_INPUT";
            if (e.getMessage().contains("마감 기한")) errorCode = "INVALID_DUE_DATE";
            if (e.getMessage().contains("스터디")) errorCode = "STUDY_NOT_FOUND";

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "code", errorCode,
                            "message", e.getMessage()
                    ));
        } catch (Exception e) {
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
            HttpSession session // 💡 변경: HttpSession 주입
    ) {
        Long userId = SessionUtil.getLoginUserId(session); // 💡 변경: 세션 유틸 적용

        if (userId == null) {
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
                    userId, // 💡 변경
                    request.content()
            );

            return ResponseEntity.ok(Map.of(
                    "message", "과제가 성공적으로 제출되었습니다.",
                    "submissionId", submissionId
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("code", "INVALID_REQUEST", "message", e.getMessage()));

        } catch (IllegalStateException e) {
            String errorCode = "SUBMISSION_REJECTED";

            if (e.getMessage().contains("마감")) errorCode = "PAST_DEADLINE";
            if (e.getMessage().contains("이미 제출")) errorCode = "ALREADY_SUBMITTED";
            if (e.getMessage().contains("멤버가 아닙니다")) errorCode = "NOT_A_MEMBER";

            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("code", errorCode, "message", e.getMessage()));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("code", "SERVER_ERROR", "message", "제출 중 오류가 발생했습니다."));
        }
    }

    @GetMapping("/my-assignments")
    public ResponseEntity<List<AssignmentResponseDTO>> getMyAssignments(
            HttpSession session // 💡 변경: HttpSession 주입
    ) {
        Long userId = SessionUtil.getLoginUserId(session); // 💡 변경: 세션 유틸 적용

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<AssignmentResponseDTO> assignments = assignmentService.getMyAssignments(userId); // 💡 변경

        return ResponseEntity.ok(assignments);
    }

    @GetMapping("/{studyId}/submissions/{assignmentId}")
    public ResponseEntity<?> getSubmissions(
            @PathVariable Long studyId,
            @PathVariable Long assignmentId,
            HttpSession session // 💡 변경: HttpSession 주입
    ) {
        Long userId = SessionUtil.getLoginUserId(session); // 💡 변경: 세션 유틸 적용

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            List<SubmissionResponseDTO> responses = assignmentService.getSubmissionsByLeader(
                    studyId, assignmentId, userId // 💡 변경
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
            HttpSession session // HttpSession 주입
    ) {
        Long userId = SessionUtil.getLoginUserId(session); //  세션 유틸 적용


        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            List<StudyLeaderGroupResponseDTO> responses = assignmentService.getAssignmentsByLeader(userId);
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/submissions/{submissionId}/grade")
    public ResponseEntity<?> gradeSubmission(
            @PathVariable Long submissionId,
            @RequestBody @Valid GradeRequestDTO dto,
            HttpSession session // 💡 변경: HttpSession 주입
    ) {
        Long userId = SessionUtil.getLoginUserId(session); // 💡 변경: 세션 유틸 적용

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            assignmentService.gradeSubmission(
                    submissionId, dto, userId // 💡 변경
            );
            return ResponseEntity.ok(Map.of("message", "채점이 완료되었습니다."));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}