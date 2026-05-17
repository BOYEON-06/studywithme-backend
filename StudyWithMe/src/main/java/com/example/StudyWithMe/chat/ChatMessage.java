package com.example.StudyWithMe.chat;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long studyId; // 어떤 스터디의 채팅인지
    private String sender; // 보낸 사람 이름
    private String message;
    private LocalDateTime timestamp;

    @Builder
    public ChatMessage(Long studyId, String sender, String message) {
        this.studyId = studyId;
        this.sender = sender;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }
}