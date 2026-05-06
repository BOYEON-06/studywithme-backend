package com.example.StudyWithMe.assignment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    // 특정 스터디의 과제 목록을 가져올 때 사용
    List<Assignment> findByStudyGroupId(Long studyGroupId);

    // 로그인한 사용자가 참여 중인 스터디의 모든 과제 조회
    @Query("SELECT a FROM Assignment a " +
            "JOIN FETCH a.studyGroup sg " +         // StudyGroup 한꺼번에 가져오기
            "JOIN FETCH sg.creator " +              // StudyGroup의 생성자까지 한꺼번에 가져오기
            "JOIN sg.participants p " +             // 참여자 조건 확인을 위한 조인 (Fetch는 안 함)
            "WHERE p.id = :memberId " +
            "ORDER BY a.dueDate ASC")
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

    // 스터디장 전용: 과제 리스트 조회 시 제출물(Submissions)까지 미리 가져오기 (성능 최적화)
    @Query("SELECT DISTINCT a FROM Assignment a " +
            "JOIN FETCH a.studyGroup sg " +
            "LEFT JOIN FETCH a.submissions s " + // 제출물이 0개일 수도 있으므로 LEFT JOIN
            "WHERE sg.creator.id = :leaderId " +
            "ORDER BY a.createdAt DESC")
    List<Assignment> findAllByLeaderId(@Param("leaderId") Long leaderId);
}
