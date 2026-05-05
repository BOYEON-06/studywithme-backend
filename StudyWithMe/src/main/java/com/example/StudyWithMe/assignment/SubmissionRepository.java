package com.example.StudyWithMe.assignment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubmissionRepository extends JpaRepository<AssignmentSubmission, Long> {

    // 1. 중복 제출 확인 (엔티티 내부의 연관관계 필드명에 맞게 작성)
    // 만약 엔티티에 'Assignment assignment' 필드가 있다면 assignmentId로 조회 가능합니다.
    boolean existsByAssignmentIdAndMemberId(Long assignmentId, Long memberId);

    // 2. 특정 과제의 모든 제출 엔티티 조회
    List<AssignmentSubmission> findByAssignmentId(Long assignmentId);

    // 3. 특정 멤버의 제출 상세 조회
    Optional<AssignmentSubmission> findByAssignmentIdAndMemberId(Long assignmentId, Long memberId);
}
