package com.example.StudyWithMe.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true, value = {"hibernateLazyInitializer", "handler"})
public class PrincipalDetails implements UserDetails, Serializable {
    private static final long serialVersionUID = 1L;

    private final MemberSessionDTO memberSessionDTO;

    // Jackson 3.x가 JSON 데이터를 읽어와 이 객체를 생성할 수 있도록 전용 생성자(JsonCreator)를 명시
    // final 필드가 있어서 기본 생성자를 만들지 못하므로, 이 방식이 가장 완벽하고 안전
    @JsonCreator
    public PrincipalDetails(@JsonProperty("memberSessionDTO") MemberSessionDTO memberSessionDTO) {
        this.memberSessionDTO = memberSessionDTO;
    }

    public MemberSessionDTO getSessionMember() {
        return memberSessionDTO;
    }

    @Override
    public String getPassword() {
        return memberSessionDTO.getPassword();
    }

    @Override
    public String getUsername() {
        return memberSessionDTO.getName();
    }

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
        // 로그인한 모든 사용자에게 'ROLE_USER' 권한을 기본으로 부여
        collect.add(new SimpleGrantedAuthority("ROLE_USER"));
        return collect;
    }
}