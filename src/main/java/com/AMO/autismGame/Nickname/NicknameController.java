package com.AMO.autismGame.Nickname;

import com.AMO.autismGame.Nickname.NicknameService;
import com.AMO.autismGame.Member.Member;
import com.AMO.autismGame.Member.MemberRepository;
import com.AMO.autismGame.security.JwtUtil;
import io.jsonwebtoken.Jwt;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class NicknameController {
    private final MemberRepository memberRepository;
    private final NicknameService nicknameService;
    private final JwtUtil jwtUtil;

    @PostMapping("/api/auth/nickname")
    public ResponseEntity<Map<String, String>> setNickname(
            @RequestHeader("Authorization") String token,
            @RequestBody Map<String, String> request) {

        // 🟢 1. JWT에서 userIdentifier 추출
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        String userIdentifier = jwtUtil.extractUserIdentifier(token);

        // 🟢 4. 닉네임 저장
        String nickname = request.get("nickname");
        Optional<Member> optionalMember = memberRepository.findByUserIdentifier(userIdentifier);

        if (optionalMember.isPresent()) {
            Member member = optionalMember.get();
            member.setNickname(nickname); // 닉네임 업데이트
            memberRepository.save(member); // DB 반영

            // 🟢 5. 성공 응답 반환
            nicknameService.initializeDefaultMapsForMember(member);

            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Nickname set successfully");
            return ResponseEntity.ok(response);
        } else {
            // 사용자가 DB에 없으면 에러 반환
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "User not found");
            return ResponseEntity.badRequest().body(response);
        }
    }
}
