package com.example.StudyWithMe.study;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import com.example.StudyWithMe.member.Member;

@Entity
@Getter @Setter
public class StudyGroup implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;       // 스터디 이름
    private String description; // 스터디 설명

    @Column(unique = true)
    private String inviteCode;  // 참여 코드 (랜덤 생성)

    // 스터디를 만든 사람 (Member 엔티티와 연결)
    @ManyToOne
    @JoinColumn(name = "creator_id")
    private Member creator;

    // 스터디에 참여 중인 멤버들
    @ManyToMany
    @JoinTable(
            name = "study_group_members", // 중간 테이블 이름
            joinColumns = @JoinColumn(name = "study_group_id"),
            inverseJoinColumns = @JoinColumn(name = "member_id")
    )
    private List<Member> participants = new ArrayList<>();

    // 스터디 생성 시 호출할 메서드
    public void generateInviteCode() {
        this.inviteCode = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
