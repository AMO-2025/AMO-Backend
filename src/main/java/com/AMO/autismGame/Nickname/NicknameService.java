package com.AMO.autismGame.Nickname;

import com.AMO.autismGame.Map.MapEntity;
import com.AMO.autismGame.Map.MapRepository;
import com.AMO.autismGame.Member.Member;
import com.AMO.autismGame.Map.MemberMap;
import com.AMO.autismGame.Map.MemberMapRepository;
import com.AMO.autismGame.Member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NicknameService {
    private final MapRepository mapRepository;
    private final MemberMapRepository memberMapRepository;

    @Transactional
    public void initializeDefaultMapsForMember(Member member) {
        List<MapEntity> maps = mapRepository.findAll();
        for (MapEntity map : maps) {
            MemberMap memberMap = new MemberMap();
            memberMap.setMember(member);
            memberMap.setMap(map);
            memberMap.setUnlocked("1".equals(map.getMapID())); // mapID가 "1"이면 true
            memberMapRepository.save(memberMap);
        }
    }
}
