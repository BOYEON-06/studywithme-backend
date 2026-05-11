package com.example.StudyWithMe.member;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    // 이름으로 회원을 찾는 쿼리 메서드
    Optional<Member> findByName(String name);

    // 이름 존재 여부 확인 쿼리 생성
    boolean existsByName(String name);
}
