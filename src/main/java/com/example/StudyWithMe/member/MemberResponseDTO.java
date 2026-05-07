package com.example.StudyWithMe.member;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class MemberResponseDTO {
    private Long id;
    private String name;
    private LocalDateTime createdAt;

    public MemberResponseDTO(Member member) {
        this.id = member.getId();
        this.name = member.getName();
        this.createdAt = member.getCreatedAt();
    }
}
