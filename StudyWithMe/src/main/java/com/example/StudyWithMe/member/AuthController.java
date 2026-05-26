package com.example.StudyWithMe.member;

import com.example.StudyWithMe.config.PrincipalDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
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
    private final HttpSessionSecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();

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
            HttpServletRequest httpRequest,
            HttpServletResponse response // response 객체 추가
    ) {
        // 1. 서비스 로직 호출
        ContinueRequestDTO result = memberService.loginOrJoin(request.getName(), request.getPassword());
        Member member = result.getMember();

        com.example.StudyWithMe.config.MemberSessionDTO sessionDto = new com.example.StudyWithMe.config.MemberSessionDTO(
                member.getId(),
                member.getName(),
                member.getPassword()
        );

        // 2. Spring Security 세션 강제 생성 (엔티티 대신 DTO 장착)
        PrincipalDetails principalDetails = new PrincipalDetails(sessionDto);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                principalDetails,
                null,
                principalDetails.getAuthorities()
        );

        // 새로운 컨텍스트 객체를 명시적으로 생성하고 인증 정보 설정
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        // 스프링 시큐리티 6 규격에 맞게 리포지토리를 통해 세션에 확실히 저장
        securityContextRepository.saveContext(context, httpRequest, response);

        httpRequest.getSession().setAttribute("LOGIN_USER", sessionDto);

        return ResponseEntity.ok(Map.of(
                "user", new MemberResponseDTO(member),
                "isNewMember", result.isNewMember(),
                "message", result.isNewMember() ? "회원가입을 환영합니다!" : "성공적으로 로그인되었습니다."
        ));
    }
}
