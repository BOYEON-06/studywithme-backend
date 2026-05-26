package com.example.StudyWithMe.config;

import com.example.StudyWithMe.member.Member;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.util.Collection;
import java.util.ArrayList;

@Getter
public class PrincipalDetails implements UserDetails, Serializable {
    private static final long serialVersionUID = 1L;

    private final Member member; // 불변성 유지

    public PrincipalDetails(Member member) {
        this.member = member;
    }

    public Member getMember() {
        return member;
    }

    // 사용자가 입력한 비번과 비교하기 위해 엔티티의 비번을 반환해야 함
    @Override
    public String getPassword() {
        return member.getPassword();
    }

    @Override
    public String getUsername() {
        return member.getName();
    }

    // 계정 상태 관련 설정 (보통은 모두 true)
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> collect = new ArrayList<>();
        // 권한이 필요하다면 아래와 같이 추가 (예: ROLE_USER)
        // collect.add(() -> "ROLE_USER");
        return collect;
    }
}