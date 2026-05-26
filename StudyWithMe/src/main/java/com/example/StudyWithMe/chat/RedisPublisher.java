package com.example.StudyWithMe.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class RedisPublisher {

    private final RedisTemplate<String, Object> redisTemplate;

    // Redis 채널(Topic)에 메시지를 발행(Publish)하는 메서드
    public void publish(ChannelTopic topic, ChatResponseDTO message) {
        redisTemplate.convertAndSend(topic.getTopic(), message);
    }
}