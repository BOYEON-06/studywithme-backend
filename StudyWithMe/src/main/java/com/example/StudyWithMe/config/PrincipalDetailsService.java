package com.example.StudyWithMe.config;

import com.example.StudyWithMe.config.PrincipalDetails;
import com.example.StudyWithMe.member.Member;
import com.example.StudyWithMe.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PrincipalDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String name) throws UsernameNotFoundException {
        // DB에서 이름(또는 이메일)으로 유저를 찾음
        Member memberEntity = memberRepository.findByName(name)
                .orElseThrow(() -> new UsernameNotFoundException("해당 유저를 찾을 수 없습니다."));

        return new PrincipalDetails(memberEntity);
    }
}
