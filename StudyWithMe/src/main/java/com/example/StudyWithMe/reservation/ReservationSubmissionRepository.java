package com.example.StudyWithMe.reservation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReservationSubmissionRepository extends JpaRepository<ReservationSubmission, Long> {
    // 필요 시: 특정 유저가 이미 해당 과제를 제출했는지 중복 체크용
    boolean existsByReservationIdAndMemberId(Long reservationId, Long memberId);
}
