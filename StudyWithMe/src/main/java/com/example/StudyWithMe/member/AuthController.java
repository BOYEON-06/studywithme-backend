package com.example.StudyWithMe.member;

import com.example.StudyWithMe.config.PrincipalDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final MemberService memberService;

    // 입력 받은 이름이 DB에 존재하는지 확인
    @GetMapping("/check-name")
    public ResponseEntity<Map<String, Object>> checkName(@RequestParam String name) {
        boolean isJoined = memberService.exists(name);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("userName", name);
        response.put("data", isJoined);
        if (isJoined) {
            response.put("message", "기존 회원입니다.");
        } else {
            response.put("message", "신규 회원입니다.");
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/continue")
    public ResponseEntity<?> login(
            @RequestBody LoginRequestDTO request,
            HttpServletRequest httpRequest // 세션 접근을 위해 추가
    ) {
        // 1. 서비스 로직 호출 (가입 또는 비밀번호 검증)
        ContinueRequestDTO result = memberService.loginOrJoin(request.getName(), request.getPassword());
        Member member = result.getMember();

        // 2. Spring Security 세션 강제 생성
        PrincipalDetails principalDetails = new PrincipalDetails(member);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                principalDetails,
                null,
                principalDetails.getAuthorities()
        );

        // SecurityContext에 인증 정보 저장
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 실제 서블릿 세션에 Spring Security 컨텍스트를 동기화
        HttpSession session = httpRequest.getSession(true);
        session.setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                SecurityContextHolder.getContext()
        );

        return ResponseEntity.ok(Map.of(
                "user", new MemberResponseDTO(member),
                "isNewMember", result.isNewMember(),
                "message", result.isNewMember() ? "회원가입을 환영합니다!" : "성공적으로 로그인되었습니다."
        ));
    }
}
