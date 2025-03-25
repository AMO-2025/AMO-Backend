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

    private final LoginService loginService;

    @PostMapping("/api/auth/identify")
    public ResponseEntity<Map<String, String>> login(@RequestBody Map<String, String> request) {
        String userIdentifier = request.get("userIdentifier");
        String phoneNumber = request.get("phoneNumber");
        Map<String, String> response = loginService.login(userIdentifier, phoneNumber);

        return "error".equals(response.get("status")) ?
                ResponseEntity.badRequest().body(response) :
                ResponseEntity.ok(response);
    }

    @GetMapping("/api/auth/validate")
    public ResponseEntity<Map<String, String>> validateToken(@RequestHeader("Authorization") String tokenHeader) {
        Map<String, String> response = loginService.validateToken(tokenHeader);
        return "error".equals(response.get("status")) ?
                ResponseEntity.status(401).body(response) :
                ResponseEntity.ok(response);
    }
}

