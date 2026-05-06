package com.example.StudyWithMe.assignment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubmissionRepository extends JpaRepository<AssignmentSubmission, Long> {

    // 1. 중복 제출 확인
    // s.member.id를 자동으로 비교하므로 기존 이름 유지 가능합니다.
    boolean existsByAssignmentIdAndMemberId(Long assignmentId, Long memberId);

    // 2. 특정 과제의 모든 제출 엔티티 조회
    // ★ 중요: 페치 조인을 추가하여 Member 정보를 한 번에 가져오게 수정합니다.
    @Query("select s from AssignmentSubmission s " +
            "join fetch s.member " +
            "where s.assignment.id = :assignmentId")
    List<AssignmentSubmission> findByAssignmentId(@Param("assignmentId") Long assignmentId);

    // 3. 스터디장이 관리하는 모든 스터디의 모든 제출물 조회 (DTO에 필요한 모든 ID를 한 번에)
    @Query("SELECT s FROM AssignmentSubmission s " +
            "JOIN FETCH s.member m " +               // 제출자 정보
            "JOIN FETCH s.assignment a " +           // 과제 정보 (submissionCount 계산용)
            "JOIN FETCH a.studyGroup sg " +          // 스터디 정보 (studyGroupId용)
            "WHERE sg.creator.id = :leaderId " +     // 방장 권한 확인
            "ORDER BY s.submittedAt DESC")
    List<AssignmentSubmission> findAllByStudyLeader(@Param("leaderId") Long leaderId);

    // 3. 특정 멤버의 제출 상세 조회
    Optional<AssignmentSubmission> findByAssignmentIdAndMemberId(Long assignmentId, Long memberId);

}
