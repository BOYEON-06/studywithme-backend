package com.example.StudyWithMe.study;

import com.example.StudyWithMe.config.SessionUtil;
import com.example.StudyWithMe.member.Member;
import com.example.StudyWithMe.member.MemberRepository;
import com.example.StudyWithMe.member.MemberStudyListDTO;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/studies")
@RequiredArgsConstructor
public class StudyController {

    private final StudyService studyService;
    private final MemberRepository memberRepository;

    @PostMapping("/create")
    public ResponseEntity<?> create(
            @RequestBody Map<String, String> request,
            HttpSession session // 💡 유지
    ) {
        // 💡 [개선] SessionUtil을 사용하여 깔끔하게 ID 한줄 요약
        Long userId = SessionUtil.getLoginUserId(session);

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        try {
            // 안전하게 찾은 ID로 순정 엔티티를 하나 복원해옵니다.
            Member currentMember = memberRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

            StudyGroup createdGroup = studyService.createStudy(
                    request.get("title"),
                    request.get("description"),
                    currentMember
            );

            return ResponseEntity.ok(Map.of(
                    "id", createdGroup.getId(),
                    "title", createdGroup.getTitle(),
                    "inviteCode", createdGroup.getInviteCode(),
                    "message", "스터디가 성공적으로 생성되었습니다!"
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("스터디 생성 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    @PostMapping("/join")
    public ResponseEntity<?> join(
            @RequestBody Map<String, String> request,
            HttpSession session // 💡 변경: @AuthenticationPrincipal 걷어내고 HttpSession 주입
    ) {
        // 💡 [변경] 시큐리티 억까 필터를 우회하기 위해 세션 유틸로 ID 추출
        Long userId = SessionUtil.getLoginUserId(session);

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        String inviteCode = request.get("inviteCode");

        try {
            // 💡 변경: 추출한 userId 전달
            String studyTitle = studyService.joinStudy(inviteCode, userId);

            return ResponseEntity.ok(Map.of(
                    "message", "스터디 참여에 성공했습니다!",
                    "studyTitle", studyTitle
            ));

        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류: " + e.getMessage());
        }
    }

    @GetMapping("/mystudylist")
    public ResponseEntity<?> getMyStudies(HttpSession session) { // 💡 유지

        System.out.println("====== [디버깅] mystudylist 요청 들어옴 ======");

        // 💡 [개선] 노가다 if-else instanceof 분기 로직을 싹 날리고 유틸로 한방에 해결!
        Long userId = SessionUtil.getLoginUserId(session);

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        try {
            // 💡 안전하게 추출한 유저 식별자 ID를 서비스에 주입합니다!
            List<MemberStudyListDTO> myStudies = studyService.getMyStudyList(userId);
            return ResponseEntity.ok(myStudies);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("목록을 불러오는 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}