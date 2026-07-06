package com.example.StudyWithMe.study;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class StudyListDTO {
    private Long id;
    private String title;
    private String description;
    private String inviteCode;
    private String creatorName;
    private List<StudyMemberDTO> participants;
}
