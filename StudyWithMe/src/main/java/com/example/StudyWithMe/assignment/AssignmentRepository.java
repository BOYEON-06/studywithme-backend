package com.example.StudyWithMe.assignment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    // 특정 스터디의 과제 목록을 가져올 때 사용
    List<Assignment> findByStudyGroupId(Long studyGroupId);

    // 로그인한 사용자가 참여 중인 스터디의 모든 과제 조회
    @Query("SELECT a FROM Assignment a " +
            "JOIN a.studyGroup sg " +
            "JOIN sg.participants p " +
            "WHERE p.id = :memberId " +
            "ORDER BY a.dueDate ASC") // 마감일 순 정렬
    List<Assignment> findAllByMemberId(@Param("memberId") Long memberId);

    // 참여 중인 스터디의 과제 중 아직 마감되지 않은 유효한 과제만 조회
    @Query("SELECT a FROM Assignment a " +
            "JOIN a.studyGroup sg " +
            "JOIN sg.participants p " +
            "WHERE p.id = :memberId AND a.dueDate > :now " +
            "ORDER BY a.dueDate ASC")
    List<Assignment> findActiveAssignmentsByMemberId(
            @Param("memberId") Long memberId,
            @Param("now") LocalDateTime now
    );
}
