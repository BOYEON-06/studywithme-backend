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
            HttpSession session
    ) {
        Long userId = SessionUtil.getLoginUserId(session);

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        try {
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
            HttpSession session
    ) {
        // 세션 유틸로 ID 추출
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

    @DeleteMapping("/{studyId}")
    public ResponseEntity<?> delete(
            @PathVariable Long studyId,
            HttpSession session
    ) {
        Long userId = SessionUtil.getLoginUserId(session);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        try {
            // 2. 서비스에 스터디 ID와 요청한 유저 ID를 함께 전달
            studyService.deleteStudy(studyId, userId);
            return ResponseEntity.noContent().build(); // 204 No Content

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage()); // 403 권한 없음
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("스터디 삭제 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    @GetMapping("/mystudylist")
    public ResponseEntity<?> getMyStudies(HttpSession session) {
        Long userId = SessionUtil.getLoginUserId(session);

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        try {
            List<StudyListDTO> myStudies = studyService.getMyStudyList(userId);
            return ResponseEntity.ok(myStudies);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("목록을 불러오는 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    @DeleteMapping("/{studyId}/members/{memberId}")
    public ResponseEntity<?> deleteMember(
            @PathVariable Long studyId,
            @PathVariable Long memberId,
            HttpSession session
    ) {
        Long loginUserId = SessionUtil.getLoginUserId(session);
        if (loginUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        try {
            studyService.removeMemberFromStudy(studyId, memberId, loginUserId);
            return ResponseEntity.ok(Map.of("message", "해당 멤버가 스터디에서 성공적으로 제외되었습니다."));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage()); // 권한 없음 (403)
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("멤버 제외 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}