package com.example.StudyWithMe.config;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StompHandler implements ChannelInterceptor {

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        // 연결 시점에 세션/인증 정보 확인 (여기서는 세션에 담긴 유저 정보를 꺼냄)
        if (StompCommand.CONNECT == accessor.getCommand()) {
            // Spring Security 세션 정보를 WebSocket 세션으로 연동하는 로직
            // 혹은 연결 시점에 권한 체크
        }
        return message;
    }
}
