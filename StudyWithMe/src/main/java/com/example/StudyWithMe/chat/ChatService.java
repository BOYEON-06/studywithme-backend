package com.example.StudyWithMe.chat;

import com.example.StudyWithMe.study.StudyGroup;
import com.example.StudyWithMe.study.StudyGroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final StudyGroupRepository studyGroupRepository;

    // 메시지 저장
    @Transactional
    public ChatMessage saveMessage(Long studyId, String sender, String content, Long currentMemberId) {

        StudyGroup study = studyGroupRepository.findById(studyId)
                .orElseThrow(() -> new IllegalArgumentException("스터디를 찾을 수 없습니다."));

        if (!studyGroupRepository.existsMemberInStudy(studyId, currentMemberId)) {
            throw new AccessDeniedException("해당 스터디의 멤버가 아닙니다.");
        }

        ChatMessage chatMessage = ChatMessage.builder()
                .studyId(studyId)
                .sender(sender)
                .message(content)
                .build();
        return chatMessageRepository.save(chatMessage);
    }

    // 이전 채팅 내역 가져오기
    public List<ChatMessage> getChatHistory(Long studyId) {
        return chatMessageRepository.findByStudyIdOrderByTimestampAsc(studyId);
    }
}