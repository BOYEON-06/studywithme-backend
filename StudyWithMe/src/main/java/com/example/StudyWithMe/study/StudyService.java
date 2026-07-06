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

    @Transactional
    public void deleteStudy(Long studyId, Long memberId) {
        StudyGroup existingStudy = studyGroupRepository.findById(studyId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 스터디입니다."));

        if (!existingStudy.getCreator().getId().equals(memberId)) {
            throw new IllegalStateException("스터디 삭제 권한이 없습니다.");
        }

        // 다대다 중간 테이블 정리를 위한 참여자 목록 비우기
        existingStudy.getParticipants().clear();


        studyGroupRepository.delete(existingStudy);
    }


    @Transactional(readOnly = true)
    public List<StudyListDTO> getMyStudyList(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저가 없습니다."));

        return member.getMyStudies().stream()
                .map(study -> {
                    List<StudyMemberDTO> participantDTOs = study.getParticipants().stream()
                            .map(p -> new StudyMemberDTO(p.getId(), p.getName()))
                            .collect(Collectors.toList());

                    return new StudyListDTO(
                            study.getId(),
                            study.getTitle(),
                            study.getDescription(),
                            study.getInviteCode(),
                            study.getCreator().getName(),
                            participantDTOs
                    );
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void removeMemberFromStudy(Long studyId, Long targetMemberId, Long loginUserId) {
        StudyGroup studyGroup = studyGroupRepository.findById(studyId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 스터디입니다."));

        if (!studyGroup.getCreator().getId().equals(loginUserId)) {
            throw new IllegalStateException("멤버를 제외할 권한이 없습니다.");
        }

        if (targetMemberId.equals(loginUserId)) {
            throw new IllegalStateException("스터디장은 자기 자신을 멤버 목록에서 제외할 수 없습니다.");
        }

        Member targetMember = memberRepository.findById(targetMemberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        boolean removed = studyGroup.getParticipants().removeIf(member -> member.getId().equals(targetMemberId));

        if (!removed) {
            throw new IllegalArgumentException("해당 회원은 이 스터디의 참여자가 아닙니다.");
        }
    }
}