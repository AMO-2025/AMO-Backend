package com.AMO.autismGame.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {
    
    @Value("${jwt.secret}")
    private String secretKey;

    private Key key;

    @PostConstruct
    public void init() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

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