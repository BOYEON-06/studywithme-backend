package com.example.StudyWithMe.member;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ContinueRequestDTO {
    private Member member;
    private boolean isNewMember;
}
