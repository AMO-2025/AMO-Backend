package com.AMO.autismGame.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {
    private static final String SECRET_KEY = "your_very_secret_key_which_should_be_long"; // 실제 환경에서는 환경 변수로 관리!

    private final Key key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());

    // ✅ JWT 토큰 생성
    public String generateToken(String userIdentifier) {
        return Jwts.builder()
                .setSubject(userIdentifier) // 사용자 식별값 (예: 보안코드)
                .setIssuedAt(new Date()) // 발행 시간
                .signWith(key, SignatureAlgorithm.HS256) // 서명 알고리즘
                .compact();
    }

    // ✅ JWT에서 userIdentifier(보안코드) 추출
    public String extractUserIdentifier(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    // ✅ JWT 유효성 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }
}