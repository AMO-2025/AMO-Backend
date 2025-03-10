package com.AMO.autismGame.security;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class AuthController {
    private final JwtUtil jwtUtil;

    @GetMapping("/api/auth/validate")
    public Map<String, String> validateToken(@RequestHeader("Authorization") String token) {
        Map<String, String> response = new HashMap<>();

        if (token.startsWith("Bearer ")) {
            token = token.substring(7); // "Bearer " 제거
        }

        if (jwtUtil.validateToken(token)) {
            response.put("status", "success");
            response.put("userIdentifier", jwtUtil.extractUserIdentifier(token));
        } else {
            response.put("status", "error");
            response.put("message", "Invalid or expired token");
        }

        return response;
    }
}