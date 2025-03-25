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
        List<MapEntity> maps = mapRepository.findAll();
        for (MapEntity map : maps) {
            MemberMap memberMap = new MemberMap();
            memberMap.setMember(member);
            memberMap.setMap(map);
            memberMap.setUnlocked("1".equals(map.getMapID()));
            memberMapRepository.save(memberMap);
        }
    }
}
