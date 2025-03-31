package com.AMO.autismGame.Map;

import com.AMO.autismGame.Member.Member;
import com.AMO.autismGame.Member.MemberRepository;
import com.AMO.autismGame.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/map")
@RequiredArgsConstructor
public class MapUnlockController {

    private final MemberRepository memberRepository;
    private final MemberMapRepository memberMapRepository;
    private final MapRepository mapRepository;
    private final JwtUtil jwtUtil;

    @PostMapping("/unlock")
    public ResponseEntity<Map<String, Object>> unlockNextMap(
            @RequestHeader("Authorization") String tokenHeader,
            @RequestBody Map<String, Object> request) {

        String token = tokenHeader.replace("Bearer ", "");
        String userIdentifier = jwtUtil.extractUserIdentifier(token);
        int clearedMapID = (int) request.get("clearedMapID");

        Optional<Member> memberOpt = memberRepository.findByUserIdentifier(userIdentifier);
        if (memberOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "User not found"
            ));
        }

        Member member = memberOpt.get();

        // 4번 맵까지라면 더 이상 해금할 맵이 없음
        if (clearedMapID >= 4) {
            return ResponseEntity.ok(Map.of(
                    "status", "clear",
                    "message", "game is done"
            ));
        }

        int nextMapID = clearedMapID + 1;
        String nextMapIDString = String.valueOf(nextMapID);

        Optional<MapEntity> nextMapOpt = mapRepository.findByMapID(nextMapIDString);
        if (nextMapOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "Next map not found"
            ));
        }

        MapEntity nextMap = nextMapOpt.get();

        // 해당 유저가 이미 가지고 있는 맵 데이터 가져오기
        Optional<MemberMap> memberMapOpt = memberMapRepository.findByMemberAndMap_MapID(member, nextMapIDString);

        if (memberMapOpt.isPresent()) {
            MemberMap memberMap = memberMapOpt.get();
            if (!memberMap.isUnlocked()) {
                memberMap.setUnlocked(true);
                memberMapRepository.save(memberMap);
            }
        } else {
            // 없으면 새로 생성
            MemberMap newUnlock = new MemberMap();
            newUnlock.setMember(member);
            newUnlock.setMap(nextMap);
            newUnlock.setUnlocked(true);
            memberMapRepository.save(newUnlock);
        }

        Map<String, Object> unlockedMapInfo = new HashMap<>();
        unlockedMapInfo.put("mapID", nextMap.getMapID());
        unlockedMapInfo.put("mapName", nextMap.getMapName());

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Next map unlocked");
        response.put("clearedMapID", clearedMapID);
        response.put("unlockedMap", unlockedMapInfo);

        return ResponseEntity.ok(response);
    }
}
