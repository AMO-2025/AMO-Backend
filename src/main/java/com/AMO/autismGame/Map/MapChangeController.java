package com.AMO.autismGame.Map;

import com.AMO.autismGame.Member.Member;
import com.AMO.autismGame.Member.MemberRepository;
import com.AMO.autismGame.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/map")
@RequiredArgsConstructor
public class MapChangeController {

    private final MemberRepository memberRepository;
    private final MemberMapRepository memberMapRepository;
    private final MapService mapService;
    private final JwtUtil jwtUtil;

    @PostMapping("/change")
    public ResponseEntity<Map<String, Object>> changeMap(
            @RequestHeader("Authorization") String tokenHeader,
            @RequestBody Map<String, Object> request) {

        String token = tokenHeader.replace("Bearer ", "");
        String userIdentifier = jwtUtil.extractUserIdentifier(token);

        // 클라이언트가 보낸 맵 ID
        Integer mapID = (Integer) request.get("mapID");

        // 서비스 처리
        Map<String, Object> response = mapService.processMapChange(userIdentifier, mapID);

        if ("error".equals(response.get("status"))) {
            return ResponseEntity.badRequest().body(response);
        } else {
            return ResponseEntity.ok(response);
        }
    }
}