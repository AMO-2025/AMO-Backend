package com.AMO.autismGame.Login;

import com.AMO.autismGame.Member.Member;
import com.AMO.autismGame.Member.MemberRepository;
import com.AMO.autismGame.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final MemberRepository memberRepository;
    private final JwtUtil jwtUtil;

    public Map<String, String> login(String userIdentifier, String phoneNumber) {
        Map<String, String> response = new HashMap<>();
        Optional<Member> checkMember = memberRepository.findByUserIdentifier(userIdentifier);

        if (checkMember.isPresent() && checkMember.get().getPhoneNumber().equals(phoneNumber)) {
            String token = jwtUtil.generateToken(userIdentifier);
            response.put("status", "success");
            response.put("message", "User identified successfully");
            response.put("token", token);
        } else {
            response.put("status", "error");
            response.put("message", "Invalid user identifier");
        }

        return response;
    }

    public Map<String, String> validateToken(String tokenHeader) {
        Map<String, String> response = new HashMap<>();
        String token = tokenHeader.replace("Bearer ", "");

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