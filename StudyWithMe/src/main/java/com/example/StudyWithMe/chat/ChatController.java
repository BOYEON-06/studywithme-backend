package com.example.StudyWithMe.chat;

import com.example.StudyWithMe.config.SessionUtil;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatController {

    private final RedisPublisher redisPublisher;
    private final ChatService chatService;
    private final ChannelTopic channelTopic;

    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat/{studyId}")
    public void sendMessage(
            @DestinationVariable Long studyId,
            @Payload Map<String, String> payload,
            SimpMessageHeaderAccessor headerAccessor
    ) {
        // 웹소켓 세션 속성에서 ID 추출
        Map<String, Object> sessionAttributes = headerAccessor.getSessionAttributes();
        if (sessionAttributes == null) {
            sendError("anonymous", "SESSION_EXPIRED", "세션이 만료되었습니다.");
            return;
        }

        Long userId = SessionUtil.getLoginUserIdFromSecurityContext(sessionAttributes);
        // 1. 로그인 체크
        if (userId == null) {
            Map<String, Object> errorPayload = new HashMap<>();
            errorPayload.put("code", "LOGIN_REQUIRED");
            errorPayload.put("message", "로그인이 필요합니다.");

            // (Object) 캐스팅 추가
            messagingTemplate.convertAndSend("/topic/errors/anonymous", (Object) errorPayload);
            return;
        }

        try {
            // 2. DB 저장
            ChatMessage savedMessage = chatService.saveMessage(
                    studyId,
                    payload.get("sender"),
                    payload.get("message"),
                    userId
            );

            // 3. Redis로 메시지 발행
            redisPublisher.publish(channelTopic, ChatResponseDTO.fromEntity(savedMessage));

        } catch (IllegalArgumentException e) {
            sendError(String.valueOf(userId), "STUDY_NOT_FOUND", e.getMessage());
        } catch (AccessDeniedException e) {
            sendError(String.valueOf(userId), "NOT_YOUR_STUDY", e.getMessage());
        } catch (Exception e) {
            log.error("채팅 전송 중 서버 오류 발생: ", e);
            sendError(String.valueOf(userId), "SERVER_ERROR", "채팅 전송 중 오류가 발생했습니다.");
        }
    }

    @GetMapping("/api/chat/{studyId}/history")
    public ResponseEntity<?> getChatHistory(
            @PathVariable Long studyId,
            HttpSession session // 💡 변경: HttpSession 주입
    ) {
        // 💡 변경: 세션 유틸 적용
        Long userId = SessionUtil.getLoginUserId(session);

        // 1. 로그인 체크
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        // 2. 권한 체크
        try {
            List<ChatResponseDTO> history = chatService.getChatHistory(studyId, userId); // 💡 변경
            return ResponseEntity.ok(history);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of(
                            "code", "NOT_YOUR_STUDY",
                            "message", e.getMessage()
                    ));
        }
    }

    private void sendError(String destinationId, String code, String message) {
        Map<String, Object> errorPayload = new HashMap<>();
        errorPayload.put("code", code);
        errorPayload.put("message", message);

        // 이렇게 호출하면 컴파일러가 고민하지 않습니다.
        messagingTemplate.convertAndSend("/topic/errors/" + destinationId, (Object) errorPayload);
    }
}