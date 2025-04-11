package com.AMO.autismGame.Npc;

import com.AMO.autismGame.Map.MapEntity;
import com.AMO.autismGame.Map.MapRepository;
import com.AMO.autismGame.Map.MemberMap;
import com.AMO.autismGame.Map.MemberMapRepository;
import com.AMO.autismGame.Member.Member;
import com.AMO.autismGame.Member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NpcService {

    private final MemberRepository memberRepository;
    private final NpcRepository npcRepository;
    private final MemberNpcRepository memberNpcRepository;
    private final MapRepository mapRepository;
    private final MemberMapRepository memberMapRepository;

    private static final int NPCS_PER_MAP = 10; // 맵당 NPC 개수

    @Transactional
    public Map<String, Object> completeNpc(String userIdentifier, String mapID, String npcID) {
        Map<String, Object> response = new HashMap<>();

        // 1. 사용자 확인
        Optional<Member> memberOpt = memberRepository.findByUserIdentifier(userIdentifier);
        if (memberOpt.isEmpty()) {
            response.put("status", "error");
            response.put("message", "User not found");
            return response;
        }

        // 2. NPC 존재 확인
        Optional<NpcEntity> npcOpt = npcRepository.findByMapIDAndNpcID(mapID, npcID);
        if (npcOpt.isEmpty()) {
            response.put("status", "error");
            response.put("message", "NPC not found");
            return response;
        }

        Member member = memberOpt.get();
        NpcEntity npc = npcOpt.get();

        // 3. 이미 클리어한 NPC인지 확인
        Optional<MemberNpc> memberNpcOpt = memberNpcRepository.findByMemberAndNpc(member, npc);
        if (memberNpcOpt.isPresent() && memberNpcOpt.get().isCleared()) {
            response.put("status", "error");
            response.put("message", "This NPC has already been cleared");
            return response;
        }

        // 4. NPC 클리어 처리
        MemberNpc memberNpc;
        if (memberNpcOpt.isPresent()) {
            memberNpc = memberNpcOpt.get();
        } else {
            memberNpc = new MemberNpc();
            memberNpc.setMember(member);
            memberNpc.setNpc(npc);
        }
        memberNpc.setCleared(true);
        memberNpcRepository.save(memberNpc);

        // 5. 해당 맵의 모든 NPC가 클리어되었는지 확인
        long clearedNpcsCount = memberNpcRepository.countByMemberAndNpc_MapIDAndIsCleared(member, mapID, true);
        boolean allNpcsCleared = clearedNpcsCount >= NPCS_PER_MAP;

        if (allNpcsCleared) {
            // 다음 맵 생성 및 해금
            int nextMapIDInt = Integer.parseInt(mapID) + 1;
            if (nextMapIDInt <= 4) { // 최대 4개 맵
                String nextMapIDString = String.valueOf(nextMapIDInt);
                Optional<MapEntity> nextMapOpt = mapRepository.findByMapID(nextMapIDString);
                
                if (nextMapOpt.isPresent()) {
                    MapEntity nextMap = nextMapOpt.get();
                    Optional<MemberMap> memberMapOpt = memberMapRepository.findByMemberAndMap_MapID(member, nextMapIDString);
                    
                    if (memberMapOpt.isPresent()) {
                        // 이미 존재하는 맵이면 해금 상태로 업데이트
                        MemberMap existingMap = memberMapOpt.get();
                        existingMap.setUnlocked(true);
                        memberMapRepository.save(existingMap);
                    } else {
                        // 새로운 맵 생성 및 해금
                        MemberMap newMemberMap = new MemberMap();
                        newMemberMap.setMember(member);
                        newMemberMap.setMap(nextMap);
                        newMemberMap.setUnlocked(true);
                        memberMapRepository.save(newMemberMap);
                    }

                    // 성공 응답 (맵 해금)
                    Map<String, Object> unlockedMap = new HashMap<>();
                    unlockedMap.put("mapID", nextMap.getMapID());
                    unlockedMap.put("mapName", nextMap.getMapName());

                    response.put("status", "success");
                    response.put("message", "NPC cleared. All NPCs in map cleared. Next map unlocked!");
                    response.put("nextMapUnlocked", true);
                    response.put("unlockedMap", unlockedMap);
                    return response;
                }
            }
        }

        // 성공 응답 (NPC만 클리어)
        response.put("status", "success");
        response.put("message", "NPC cleared successfully");
        response.put("nextMapUnlocked", false);
        return response;
    }
} 