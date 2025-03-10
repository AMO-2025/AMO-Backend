package com.AMO.autismGame.Login;

import com.AMO.autismGame.Member.Member;
import com.AMO.autismGame.Member.MemberRepository;
import com.AMO.autismGame.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class LoginController {
    private final MemberRepository memberRepository;
    private final JwtUtil jwtUtil;

    @PostMapping("/api/auth/identify")
    public ResponseEntity<Map<String, String>> login(
            @RequestBody Map<String, String> request) {

        String userIdentifier = request.get("userIdentifier");
        String phoneNumber = request.get("phoneNumber");
        Map<String, String> response = new HashMap<>();

        Optional<Member> checkMember = memberRepository.findByUserIdentifier(userIdentifier);

        if (checkMember.isPresent() && checkMember.get().getPhoneNumber().equals(phoneNumber)) {
            String token = jwtUtil.generateToken(userIdentifier);
            response.put("status", "success");
            response.put("message", "User identified successfully");
            response.put("token", token);
            return ResponseEntity.ok(response);  // ✅ ResponseEntity 사용하여 반환
        } else {
            response.put("status", "error");
            response.put("message", "Invalid user identifier");
            return ResponseEntity.badRequest().body(response);  // ✅ 실패 시 400 Bad Request 반환
        }
    }
}

