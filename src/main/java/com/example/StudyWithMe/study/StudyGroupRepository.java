package com.example.StudyWithMe.study;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param; // 이 패키지여야 합니다!

import java.util.Optional;

@Repository
public interface StudyGroupRepository extends JpaRepository<StudyGroup, Long> {
    // 특정 스터디(studyId)에 특정 멤버(memberId)가 participants 리스트에 포함되어 있는지 확인
    @Query("SELECT COUNT(sg) > 0 FROM StudyGroup sg JOIN sg.participants p " +
            "WHERE sg.id = :studyId AND p.id = :memberId")
    boolean existsMemberInStudy(@Param("studyId") Long studyId, @Param("memberId") Long memberId);

    Optional<StudyGroup> findByInviteCode(String inviteCode);
}