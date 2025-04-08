package com.AMO.autismGame.Emotion;

import com.AMO.autismGame.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/emotion")
@RequiredArgsConstructor
public class EmotionController {

    private final EmotionService emotionService;
    private final JwtUtil jwtUtil;

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadEmotionPhoto(
            @RequestHeader("Authorization") String tokenHeader,
            @RequestParam("file") MultipartFile file,
            @RequestParam("emotion") String emotion) {
            
        String token = tokenHeader.replace("Bearer ", "");
        String userIdentifier = jwtUtil.extractUserIdentifier(token);
        
        Map<String, String> response = emotionService.saveEmotionPhoto(userIdentifier, file, emotion);
        
        if ("error".equals(response.get("status"))) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }
} 