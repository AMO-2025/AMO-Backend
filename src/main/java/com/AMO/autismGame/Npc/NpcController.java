package com.AMO.autismGame.Npc;

import com.AMO.autismGame.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/npc")
@RequiredArgsConstructor
public class NpcController {

    private final NpcService npcService;
    private final JwtUtil jwtUtil;

    @PostMapping("/complete")
    public ResponseEntity<Map<String, Object>> completeNpc(
            @RequestHeader("Authorization") String tokenHeader,
            @RequestBody Map<String, String> request) {

        String token = tokenHeader.replace("Bearer ", "");
        String userIdentifier = jwtUtil.extractUserIdentifier(token);
        String mapID = request.get("mapID");
        String npcID = request.get("npcID");

        Map<String, Object> response = npcService.completeNpc(userIdentifier, mapID, npcID);

        if ("error".equals(response.get("status"))) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }
} 