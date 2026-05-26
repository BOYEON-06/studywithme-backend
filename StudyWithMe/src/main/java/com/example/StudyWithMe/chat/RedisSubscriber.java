package com.example.StudyWithMe.chat;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@RequiredArgsConstructor
@Service
public class RedisSubscriber implements MessageListener {
    
    private final ObjectMapper redisObjectMapper;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            // 1. Redis에서 받은 메시지를 역직렬화
            String publishMessage = new String(message.getBody());
            ChatResponseDTO chatDto = redisObjectMapper.readValue(publishMessage, ChatResponseDTO.class);

            // 2. 해당 스터디방을 구독 중인 웹소켓 유저들에게 전달
            messagingTemplate.convertAndSend("/topic/study/" + chatDto.getStudyId(), chatDto);
        } catch (Exception e) {
            log.error("Redis Subscriber Error: ", e);
        }
    }
}
