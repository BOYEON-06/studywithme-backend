package com.example.StudyWithMe.config;

import jakarta.servlet.http.HttpSession;
import java.util.Map;

public class SessionUtil {

    public static Long getLoginUserId(HttpSession session) {
        Object loginUserObj = session.getAttribute("LOGIN_USER");
        if (loginUserObj == null) {
            return null;
        }

        // 레디스가 Map으로 뭉개든 DTO 정품으로 돌려주든 무조건 ID를 추출해내는 치트키 로직
        if (loginUserObj instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) loginUserObj;
            return Long.valueOf(map.get("id").toString());
        } else if (loginUserObj instanceof MemberSessionDTO) {
            return ((MemberSessionDTO) loginUserObj).getId();
        }

        return null;
    }

    public static Long getLoginUserIdFromSecurityContext() {
        org.springframework.security.core.Authentication auth =
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }

        Object principal = auth.getPrincipal();
        if (principal instanceof PrincipalDetails) {
            return ((PrincipalDetails) principal).getSessionMember().getId();
        }

        return null;
    }

    // 웹소켓 세션에서 userId를 꺼내오는 메서드 추가
    public static Long getLoginUserIdFromSecurityContext(Map<String, Object> sessionAttributes) {
        // 인터셉터가 복사해 넣은 세션 데이터를 여기서 확인
        return (Long) sessionAttributes.get("userId");
    }
}
