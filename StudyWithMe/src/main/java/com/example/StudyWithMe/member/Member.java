package com.example.StudyWithMe.member;

import com.example.StudyWithMe.study.StudyGroup;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.io.Serializable;

@Entity
@Getter
@NoArgsConstructor
public class Member implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String name;

    @JsonIgnore
    private String password;

    // 가입 시간
    @JsonIgnore
    private LocalDateTime createdAt;

    // 현재 가입 중인 스터디
    @JsonIgnore
    @ManyToMany(mappedBy = "participants")
    private List<StudyGroup> myStudies = new ArrayList<>();

    public Member(String name) {
        this.name = name;
        this.createdAt = LocalDateTime.now();
    }

    public Member(String name, String password) {
        this.name = name;
        this.password = password;
        this.createdAt = LocalDateTime.now();
    }
}

