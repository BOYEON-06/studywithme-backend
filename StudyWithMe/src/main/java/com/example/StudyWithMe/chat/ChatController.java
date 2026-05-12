package com.example.StudyWithMe.chat;

import com.example.StudyWithMe.config.PrincipalDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;

    @MessageMapping("/chat/{studyId}")
    public ResponseEntity<?> sendMessage(
            @DestinationVariable Long studyId,
            @Payload Map<String, String> payload,
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        if (principalDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("code", "LOGIN_REQUIRED", "message", "로그인이 필요합니다."));
        }

        try {
            ChatMessage savedMessage = chatService.saveMessage(
                    studyId,
                    payload.get("sender"),
                    payload.get("message"),
                    principalDetails.getMember().getId()
            );

            // 저장된 메시지 정보를 그대로 브로드캐스팅
            messagingTemplate.convertAndSend("/topic/study/" + studyId, savedMessage);
            return ResponseEntity.ok(Map.of(
                    "message", "채팅 전송 성공"
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "code", "STUDY_NOT_FOUND",
                            "message", e.getMessage()
                    ));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of(
                            "code", "NOT_YOUR_STUDY",
                            "message", e.getMessage()
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

    @GetMapping("/api/chat/{studyId}/history")
    public ResponseEntity<?> getChatHistory(
            @PathVariable Long studyId,
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        // 1. 로그인 체크
        if (principalDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        // 2. 권한 체크
        // chatService에 권한 체크 로직을 공통화 하는 게 나을 것 같음 (추후 변경 필요)
        try {
            List<ChatResponseDTO> history = chatService.getChatHistory(studyId, principalDetails.getMember().getId());
            return ResponseEntity.ok(history);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of(
                            "code", "NOT_YOUR_STUDY",
                            "message", e.getMessage()
                    ));
        }
    }
}
