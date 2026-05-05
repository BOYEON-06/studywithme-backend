package com.example.StudyWithMe.study;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface StudyGroupRepository extends JpaRepository<StudyGroup, Long> {
    Optional<StudyGroup> findByInviteCode(String inviteCode);
}