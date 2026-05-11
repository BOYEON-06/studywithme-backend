package com.example.StudyWithMe.member;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LoginRequestDTO {
    private String name;
    private String password;
}
