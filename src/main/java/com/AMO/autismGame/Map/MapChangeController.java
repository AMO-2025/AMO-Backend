package com.AMO.autismGame.Map;

import com.AMO.autismGame.Member.Member;
import com.AMO.autismGame.Member.MemberRepository;
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

    @PostMapping("/change")
    public ResponseEntity<Map<String, Object>> changeMap(@RequestBody Map<String, Object> request) {
        // JWT 인증된 사용자 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userIdentifier = authentication.getName(); // JWT 안에 담긴 userIdentifier

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