package com.example.StudyWithMe.chat;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    // 특정 스터디 그룹의 모든 메시지를 시간순으로 조회
    List<ChatMessage> findByStudyIdOrderByTimestampAsc(Long studyId);
}
