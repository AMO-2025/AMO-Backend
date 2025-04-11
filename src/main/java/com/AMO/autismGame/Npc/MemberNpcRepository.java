package com.AMO.autismGame.Npc;

import com.AMO.autismGame.Member.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface MemberNpcRepository extends JpaRepository<MemberNpc, Integer> {
    Optional<MemberNpc> findByMemberAndNpc(Member member, NpcEntity npc);
    List<MemberNpc> findByMemberAndNpc_MapID(Member member, String mapID);
    long countByMemberAndNpc_MapIDAndIsCleared(Member member, String mapID, boolean isCleared);
} 