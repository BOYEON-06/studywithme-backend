package com.example.StudyWithMe.member;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MemberStudyListDTO {
    private Long id;
    private String title;
    private String description;
    private String inviteCode;
    private String creatorName;
}
