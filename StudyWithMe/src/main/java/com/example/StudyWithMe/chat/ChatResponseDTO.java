package com.example.StudyWithMe.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.format.DateTimeFormatter;

@Getter
@Builder
@AllArgsConstructor
public class ChatResponseDTO {
    private Long id;
    private Long studyId;
    private String sender;
    private String message;
    private String timestamp; // 프론트엔드에서 읽기 편하게 String으로 변환

    // Entity를 DTO로 변환하는 정적 팩토리 메서드
    public static ChatResponseDTO fromEntity(ChatMessage entity) {
        return ChatResponseDTO.builder()
                .id(entity.getId())
                .studyId(entity.getStudyId())
                .sender(entity.getSender())
                .message(entity.getMessage())
                // 포맷 예시: 2026-05-12 10:30:15
                .timestamp(entity.getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .build();
    }
}