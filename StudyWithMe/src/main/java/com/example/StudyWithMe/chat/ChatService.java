package com.example.StudyWithMe.chat;

import com.example.StudyWithMe.study.StudyGroup;
import com.example.StudyWithMe.study.StudyGroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

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
    @Transactional(readOnly = true)
    public List<ChatResponseDTO> getChatHistory(Long studyId, Long currentMemberId) {
        // 해당 스터디의 멤버인지 확인
        if (!studyGroupRepository.existsMemberInStudy(studyId, currentMemberId)) {
            throw new AccessDeniedException("해당 스터디의 멤버가 아닙니다.");
        }

        // 시간순으로 전체 내역 조회 (필요시 페이징 처리)
        return chatMessageRepository.findByStudyIdOrderByTimestampAsc(studyId)
                .stream()
                .map(ChatResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }
}