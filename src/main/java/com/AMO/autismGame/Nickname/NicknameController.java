package com.AMO.autismGame.Nickname;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class NicknameController {

    private final NicknameService nicknameService;

    @PostMapping("/api/auth/nickname")
    public ResponseEntity<Map<String, String>> setNickname(
            @RequestHeader("Authorization") String token,
            @RequestBody Map<String, String> request) {

        String nickname = request.get("nickname");
        Map<String, String> response = nicknameService.setNickname(token, nickname);

        return "error".equals(response.get("status")) ?
                ResponseEntity.badRequest().body(response) :
                ResponseEntity.ok(response);
    }
}