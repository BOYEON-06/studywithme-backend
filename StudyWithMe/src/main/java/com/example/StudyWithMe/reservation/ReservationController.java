package com.example.StudyWithMe.reservation;

import com.example.StudyWithMe.ai.AiAssignmentResponseDTO;
import com.example.StudyWithMe.ai.AiParameterDTO;
import com.example.StudyWithMe.ai.GeminiService;
import com.example.StudyWithMe.config.PrincipalDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
@RestController
@RequestMapping("/api/reservation-tasks")
@RequiredArgsConstructor
public class ReservationController {

    private final GeminiService geminiService;
    private final ReservationService reservationService; // 서비스명 통일

    // 스터디장 전용: 스터디 컨텍스트 내에서 AI 기능을 다루는 컨트롤러
    @PostMapping("/generate-ai")
    public ResponseEntity<?> getAiSuggestion(
            @RequestBody AiParameterDTO request,
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        if (principalDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("code", "LOGIN_REQUIRED", "message", "로그인이 필요합니다."));
        }
        try {
            AiAssignmentResponseDTO suggestion = geminiService.generateAssignment(request);
            return ResponseEntity.ok(suggestion);
        } catch (Exception e) {
            throw new RuntimeException("AI API 호출 실패: " + e.getMessage());
        }
    }

    @PostMapping("/{studyId}/confirm-ai")
    public ResponseEntity<?> confirmAiReservationTask(
            @PathVariable Long studyId,
            @RequestBody ReservationRequestDTO dto,
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        if (principalDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "로그인이 필요합니다."));
        }

        try {
            Long taskId = reservationService.createReservationTask(
                    studyId,
                    dto,
                    principalDetails.getMember().getId()
            );

            return ResponseEntity.ok(Map.of(
                    "message", "실시간 예약 과제가 성공적으로 등록되었습니다.",
                    "reservationTaskId", taskId
            ));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("code", "NOT_LEADER", "message", e.getMessage()));
        } catch (IllegalArgumentException e) {
            String errorCode = "INVALID_INPUT";
            if (e.getMessage().contains("시간") || e.getMessage().contains("기한")) errorCode = "INVALID_TIME_RANGE";
            if (e.getMessage().contains("스터디")) errorCode = "STUDY_NOT_FOUND";

            return ResponseEntity.badRequest().body(Map.of("code", errorCode, "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("code", "SERVER_ERROR", "message", "서버 오류"));
        }
    }

    // 스터디원 전용
    @GetMapping("/{taskId}")
    public ResponseEntity<ReservationResponseDTO> getLiveTask(@PathVariable Long taskId) {
        ReservationResponseDTO response = reservationService.getTaskWithValidation(taskId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{taskId}/submissions")
    public ResponseEntity<Void> submitLiveTask(
            @PathVariable Long taskId,
            @RequestParam Long userId, // 필요시 @AuthenticationPrincipal로 변경 가능
            @RequestBody ReservationSubmitRequestDTO request) {

        reservationService.submitTask(taskId, userId, request);
        return ResponseEntity.ok().build();
    }
}