package com.AMO.autismGame.Nickname;

import com.AMO.autismGame.Map.MapEntity;
import com.AMO.autismGame.Map.MapRepository;
import com.AMO.autismGame.Member.Member;
import com.AMO.autismGame.Map.MemberMap;
import com.AMO.autismGame.Map.MemberMapRepository;
import com.AMO.autismGame.Member.MemberRepository;
import com.AMO.autismGame.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NicknameService {

    private final MemberRepository memberRepository;
    private final MapRepository mapRepository;
    private final MemberMapRepository memberMapRepository;
    private final JwtUtil jwtUtil;

    public Map<String, String> setNickname(String tokenHeader, String nickname) {
        Map<String, String> response = new HashMap<>();

        String token = tokenHeader.replace("Bearer ", "");
        String userIdentifier = jwtUtil.extractUserIdentifier(token);

        Optional<Member> optionalMember = memberRepository.findByUserIdentifier(userIdentifier);
        if (optionalMember.isPresent()) {
            Member member = optionalMember.get();
            member.setNickname(nickname);
            memberRepository.save(member);
            initializeDefaultMapsForMember(member);

            response.put("status", "success");
            response.put("message", "Nickname set successfully");
        } else {
            response.put("status", "error");
            response.put("message", "User not found");
        }

        return response;
    }

    @Transactional
    public void initializeDefaultMapsForMember(Member member) {
        // 이미 초기화되었는지 확인
        boolean alreadyInitialized = memberMapRepository.existsByMember(member);

        if (alreadyInitialized) {
            return; // 이미 있으면 아무 것도 하지 않음
        }

        // 없는 경우에만 4개 생성
        List<MapEntity> maps = mapRepository.findAll();
        for (MapEntity map : maps) {
            MemberMap memberMap = new MemberMap();
            memberMap.setMember(member);
            memberMap.setMap(map);
            // 첫 번째 맵(집)은 기본적으로 열려있고, 나머지는 잠겨있음
            memberMap.setUnlocked("1".equals(map.getMapID()));
            memberMapRepository.save(memberMap);
        }
    }
}
