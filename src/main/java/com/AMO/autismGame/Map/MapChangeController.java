package com.AMO.autismGame.Map;

import com.AMO.autismGame.Member.Member;
import com.AMO.autismGame.Member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
        String userIdentifier = (String) request.get("userIdentifier");
        Integer mapID = (Integer) request.get("mapID");

        Map<String, Object> response = mapService.processMapChange(userIdentifier, mapID);
        if ("error".equals(response.get("status"))) {
            return ResponseEntity.badRequest().body(response);
        } else {
            return ResponseEntity.ok(response);
        }
    }
}