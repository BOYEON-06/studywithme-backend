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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatController {

    private final RedisPublisher redisPublisher;
    private final ChatService chatService;
    private final ChannelTopic channelTopic;

    @MessageMapping("/chat/{studyId}")
    public void sendMessage(
            @DestinationVariable Long studyId,
            @Payload Map<String, String> payload
    ) {
        Long userId = SessionUtil.getLoginUserIdFromSecurityContext();

        if (userId == null) {
            // 웹소켓에서는 에러를 알리려면 특정 경로로 에러 메시지를 발행해야 합니다.
            // 일단은 여기서는 무시하거나 로그를 찍는 것이 안전합니다.
            return;
        }

        try {
            // 1. DB 저장
            ChatMessage savedMessage = chatService.saveMessage(
                    studyId,
                    payload.get("sender"),
                    payload.get("message"),
                    userId
            );

            // 2. Redis로 메시지 발행 (이게 구독자들에게 전파됨)
            redisPublisher.publish(channelTopic, ChatResponseDTO.fromEntity(savedMessage));

        } catch (Exception e) {
            // 웹소켓 처리 중 에러 발생 시 로그 기록
            log.error("채팅 전송 실패: ", e);
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
}