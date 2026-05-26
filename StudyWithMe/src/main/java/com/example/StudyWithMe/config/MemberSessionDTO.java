package com.example.StudyWithMe.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@NoArgsConstructor  // 💡 Jackson이 레디스에서 꺼내 복원할 때 쓸 기본 생성자
@AllArgsConstructor // 로그인 성공 시 생성할 때 쓸 생성자
public class MemberSessionDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private String password; // 시큐리티 패스워드 검증 및 유지용
}