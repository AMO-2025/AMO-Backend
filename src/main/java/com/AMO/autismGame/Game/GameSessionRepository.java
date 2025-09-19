package com.AMO.autismGame.Game;

import com.AMO.autismGame.Member.Member;
import com.AMO.autismGame.Npc.NpcEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface GameSessionRepository extends JpaRepository<GameSession, Long> {
    
    // 특정 회원의 게임 세션 조회
    List<GameSession> findByMember(Member member);
    
    // 특정 NPC의 게임 세션 조회
    List<GameSession> findByNpc(NpcEntity npc);
    
    // 특정 맵의 게임 세션 조회
    @Query("SELECT gs FROM GameSession gs WHERE gs.npc.mapID = :mapID")
    List<GameSession> findByMapID(@Param("mapID") String mapID);
    
    // 특정 회원과 NPC의 게임 세션 조회
    List<GameSession> findByMemberAndNpc(Member member, NpcEntity npc);
    
    // 특정 게임 타입의 세션 조회
    List<GameSession> findByGameType(GameType gameType);
    
    // 특정 회원의 특정 게임 타입 세션 조회
    List<GameSession> findByMemberAndGameType(Member member, GameType gameType);
    
    // 특정 회원의 정답률 조회
    @Query("SELECT COUNT(gs) FROM GameSession gs WHERE gs.member = :member AND gs.isCorrect = true")
    long countCorrectAnswersByMember(@Param("member") Member member);
    
    // 특정 회원의 전체 게임 수 조회
    @Query("SELECT COUNT(gs) FROM GameSession gs WHERE gs.member = :member")
    long countTotalGamesByMember(@Param("member") Member member);
}
