package com.example.StudyWithMe.config;

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
        // DB에서 이름으로 유저를 찾음
        Member member = memberRepository.findByName(name)
                .orElseThrow(() -> new UsernameNotFoundException("유저를 찾을 수 없습니다: " + name));

        // 2. 💡 엔티티의 알맹이만 쏙 빼서 직렬화 안전 지대인 DTO로 변환!
        MemberSessionDTO sessionDto = new MemberSessionDTO(
                member.getId(),
                member.getName(),
                member.getPassword()
        );

        // 3. DTO를 장착한 인증 객체 반환
        return new PrincipalDetails(sessionDto);
    }
}
