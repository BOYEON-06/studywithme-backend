package com.example.StudyWithMe.member;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Transactional
    public ContinueRequestDTO loginOrJoin(String name, String rawPassword) {
        Optional<Member> memberOpt = memberRepository.findByName(name);

        if (memberOpt.isPresent()) {
            // 1. 기존 유저인 경우: 비밀번호 검증
            Member member = memberOpt.get();
            if (!passwordEncoder.matches(rawPassword, member.getPassword())) {
                throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
            }
            return new ContinueRequestDTO(member, false); // 기존 유저
        } else {
            // 2. 신규 유저인 경우: 암호화 후 가입
            String encodedPassword = passwordEncoder.encode(rawPassword);
            Member newMember = memberRepository.save(new Member(name, encodedPassword));
            return new ContinueRequestDTO(newMember, true); // 신규 유저
        }
    }

    @Transactional
    public boolean exists(String name) {
        return memberRepository.existsByName(name);
    }

}
