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

    public Map<String, Object> processMapChange(String userIdentifier, Integer mapID) {
        Map<String, Object> response = new HashMap<>();

        Optional<Member> memberOptional = memberRepository.findByUserIdentifier(userIdentifier);
        if (memberOptional.isEmpty()) {
            response.put("status", "error");
            response.put("message", "User not found");
            return response;
        }

        Member member = memberOptional.get();
        List<MemberMap> memberMaps = memberMapRepository.findByMember(member);

        for (MemberMap memberMap : memberMaps) {
            if (memberMap.getMap().getId() == mapID) {
                if (memberMap.isUnlocked()) {
                    Map<String, Object> selectedMap = new HashMap<>();
                    selectedMap.put("mapID", memberMap.getMap().getMapID());
                    selectedMap.put("mapName", memberMap.getMap().getMapName());

                    response.put("status", "success");
                    response.put("message", "Map changed successfully");
                    response.put("selectedMap", selectedMap);
                } else {
                    response.put("status", "error");
                    response.put("message", "Map is locked. Complete previous stage to unlock.");
                }
                return response;
            }
        }

        response.put("status", "error");
        response.put("message", "Map not found");
        return response;
    }


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
