package com.AMO.autismGame.Map;

import com.AMO.autismGame.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/map")
@RequiredArgsConstructor
public class MapController {

    private final MapService mapService;
    private final JwtUtil jwtUtil;

    @GetMapping("/select")
    public ResponseEntity<Map<String, Object>> selectMap(@RequestHeader("Authorization") String tokenHeader) {
        String token = tokenHeader.replace("Bearer ", "");
        String userIdentifier = jwtUtil.extractUserIdentifier(token);
        Map<String, Object> response = mapService.getMapInfo(userIdentifier);
        return ResponseEntity.ok(response);
    }
}
