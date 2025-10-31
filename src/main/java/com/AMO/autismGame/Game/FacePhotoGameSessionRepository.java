package com.AMO.autismGame.Game;

import com.AMO.autismGame.Member.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FacePhotoGameSessionRepository extends JpaRepository<FacePhotoGameSession, Long> {
    long countByMember(Member member);
    long countByMemberAndIsCorrect(Member member, boolean isCorrect);
    List<FacePhotoGameSession> findByMember(Member member);
    List<FacePhotoGameSession> findByMemberAndNpc_MapID(Member member, String mapID);
}
