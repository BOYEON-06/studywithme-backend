package com.example.StudyWithMe.study;

import com.example.StudyWithMe.config.PrincipalDetails;
import com.example.StudyWithMe.member.Member;
import com.example.StudyWithMe.member.MemberStudyListDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

@RestController
@RequestMapping("/api/studies")
@RequiredArgsConstructor
public class StudyController {

    private final StudyService studyService;

    @PostMapping("/create")
    public ResponseEntity<?> create(
            @RequestBody Map<String, String> request,
            @AuthenticationPrincipal PrincipalDetails principalDetails // 현재 로그인한 유저 정보 자동 주입
    ) {

        if (principalDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        try {
            // 2. 스터디 생성 서비스 호출
            StudyGroup createdGroup = studyService.createStudy(
                    request.get("title"),
                    request.get("description"),
                    principalDetails.getMember()
            );

            // 3. Entity 대신 필요한 정보만 Map이나 DTO에 담아 반환 (순환 참조 방지)
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
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        if (principalDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        String inviteCode = request.get("inviteCode");

        try {
            // 2. 서비스 호출 (Member 객체 대신 ID 전달)
            String studyTitle = studyService.joinStudy(inviteCode, principalDetails.getMember().getId());

            return ResponseEntity.ok(Map.of(
                    "message", "스터디 참여에 성공했습니다!",
                    "studyTitle", studyTitle
            ));

        } catch (IllegalStateException e) {
            // "이미 참여 중인 스터디입니다." (400)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());

        } catch (IllegalArgumentException e) {
            // "올바르지 않은 초대 코드입니다." (404)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());

        } catch (Exception e) {
            // 서버 로그 확인용 (무엇 때문에 500이 났는지 로그를 찍어보세요)
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류: " + e.getMessage());
        }
    }

    @GetMapping("/mystudylist")
    public ResponseEntity<?> getMyStudies(@AuthenticationPrincipal PrincipalDetails principalDetails) {
        if (principalDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        try {
            // 서비스에서 현재 로그인한 유저의 참여 스터디 목록 조회
            List<MemberStudyListDTO> myStudies = studyService.getMyStudyList(principalDetails.getMember().getId());
            return ResponseEntity.ok(myStudies);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("목록을 불러오는 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

}