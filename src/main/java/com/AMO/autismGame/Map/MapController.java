package com.AMO.autismGame.Map;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/map")
@RequiredArgsConstructor
public class MapController {

    private final MapService mapService;

    @GetMapping("/select")
    public ResponseEntity<Map<String, Object>> getUnlockedMaps(@RequestBody Map<String, String> request) {
        String userIdentifier = request.get("userIdentifier");
        Map<String, Object> response = mapService.getUnlockedMapInfo(userIdentifier);

        if ("error".equals(response.get("status"))) {
            return ResponseEntity.badRequest().body(response);
        }

        return ResponseEntity.ok(response);
    }
}
