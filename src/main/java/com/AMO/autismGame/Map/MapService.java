package com.AMO.autismGame.Map;

import com.AMO.autismGame.Member.Member;
import com.AMO.autismGame.Member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class MapService {

    private final MemberRepository memberRepository;
    private final MemberMapRepository memberMapRepository;

    public Map<String, Object> getUnlockedMapInfo(String userIdentifier) {
        Map<String, Object> response = new HashMap<>();

        Optional<Member> memberOptional = memberRepository.findByUserIdentifier(userIdentifier);
        if (memberOptional.isEmpty()) {
            response.put("status", "error");
            response.put("message", "User not found");
            return response;
        }

        Member member = memberOptional.get();
        List<MemberMap> memberMaps = memberMapRepository.findByMember(member);

        List<Map<String, Object>> mapList = new ArrayList<>();
        for (MemberMap memberMap : memberMaps) {
            Map<String, Object> mapData = new HashMap<>();
            mapData.put("mapID", memberMap.getMap().getMapID());
            mapData.put("mapName", memberMap.getMap().getMapName());
            mapData.put("isUnlocked", memberMap.isUnlocked());
            mapList.add(mapData);
        }

        response.put("status", "success");
        response.put("availableMaps", mapList);
        return response;
    }
}
