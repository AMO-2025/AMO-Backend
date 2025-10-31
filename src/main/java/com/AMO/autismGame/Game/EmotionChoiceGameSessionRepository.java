package com.AMO.autismGame.Game;

import com.AMO.autismGame.Member.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmotionChoiceGameSessionRepository extends JpaRepository<EmotionChoiceGameSession, Long> {
    long countByMember(Member member);
    long countByMemberAndIsCorrect(Member member, boolean isCorrect);
    List<EmotionChoiceGameSession> findByMember(Member member);
    List<EmotionChoiceGameSession> findByMemberAndNpc_MapID(Member member, String mapID);
}
