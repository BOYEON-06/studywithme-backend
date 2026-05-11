package com.example.StudyWithMe.study;

import com.example.StudyWithMe.member.Member;
import com.example.StudyWithMe.member.MemberRepository;
import com.example.StudyWithMe.member.MemberStudyListDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudyService {

    private final StudyGroupRepository studyGroupRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public StudyGroup createStudy(String title, String description, Member member) {
        StudyGroup studyGroup = new StudyGroup();
        studyGroup.setTitle(title);
        studyGroup.setDescription(description);
        studyGroup.generateInviteCode(); // 8자리 랜덤 코드 생성
        studyGroup.setCreator(member);
        studyGroup.getParticipants().add(member); // 스터디 참여자 목록에 스터디장도 추가

        return studyGroupRepository.save(studyGroup);
    }

    @Transactional
    public String joinStudy(String inviteCode, Long memberId) {
        // 1. 초대 코드로 스터디 찾기
        StudyGroup studyGroup = studyGroupRepository.findByInviteCode(inviteCode)
                .orElseThrow(() -> new IllegalArgumentException("올바르지 않은 초대 코드입니다."));

        // 2. 멤버를 영속성 컨텍스트에 다시 로드 (Lazy 에러 방지)
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException(" 사용자를 찾을 수 없습니다."));

        // 3. 중복 참여 체크 (ID로 비교)
        boolean isAlreadyJoined = studyGroup.getParticipants().stream()
                .anyMatch(p -> p.getId().equals(member.getId()));

        if (isAlreadyJoined) {
            throw new IllegalStateException("이미 참여 중인 스터디입니다.");
        }

        // 4. 참여 추가
        studyGroup.getParticipants().add(member);

        // 5. 성공 시 보여줄 스터디 제목 반환
        return studyGroup.getTitle();
    }

    @Transactional(readOnly = true)
    public List<MemberStudyListDTO> getMyStudyList(Long memberId) {
        // 1. 세션이 살아있는 상태에서 다시 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저가 없습니다."));

        // 2. 세션이 열려있으므로 Lazy Loading이 정상 작동함
        return member.getMyStudies().stream()
                .map(study -> new MemberStudyListDTO(
                        study.getId(),
                        study.getTitle(),
                        study.getDescription(),
                        study.getInviteCode(),
                        study.getCreator().getName()
                ))
                .collect(Collectors.toList());
    }
}