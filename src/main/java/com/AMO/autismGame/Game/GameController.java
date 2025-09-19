package com.AMO.autismGame.Game;

import com.AMO.autismGame.Game.dto.GameRequestDto;
import com.AMO.autismGame.Game.dto.GameResponseDto;
import com.AMO.autismGame.Game.dto.GameStatisticsDto;
import com.AMO.autismGame.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/game")
@RequiredArgsConstructor
@Slf4j
public class GameController {

    private final GameService gameService;
    private final JwtUtil jwtUtil;

    /**
     * 게임 실행 API
     * 게임 1번: 감정 선택 게임
     * 게임 2번: 얼굴 촬영 게임
     */
    @PostMapping("/play")
    public ResponseEntity<GameResponseDto> playGame(
            @RequestHeader("Authorization") String tokenHeader,
            @RequestBody GameRequestDto requestDto) {
        
        String token = tokenHeader.replace("Bearer ", "");
        String userIdentifier = jwtUtil.extractUserIdentifier(token);
        
        // 🔍 디버깅 로그 추가
        log.info("🔍 DEBUG: 요청 본문 전체 내용 = {}", requestDto);
        log.info("🔍 DEBUG: requestDto.isCorrect() 값 = {}", requestDto.isCorrect());
        log.info("🔍 DEBUG: requestDto.getGameType() 값 = {}", requestDto.getGameType());
        log.info("🔍 DEBUG: requestDto.getTargetEmotion() 값 = {}", requestDto.getTargetEmotion());
        log.info("🔍 DEBUG: requestDto.getUserEmotion() 값 = {}", requestDto.getUserEmotion());
        
        // 🔍 추가 디버깅: 필드별 상세 정보
        log.info("🔍 DEBUG: isCorrect 필드 타입 = {}", requestDto.isCorrect() ? "true" : "false");
        log.info("🔍 DEBUG: isCorrect 필드 원시값 = {}", Boolean.valueOf(requestDto.isCorrect()));
        log.info("🔍 DEBUG: 모든 필드 null 체크 = npcID:{}, mapID:{}, gameType:{}, targetEmotion:{}, userEmotion:{}, isCorrect:{}, imageBase64:{}, confidence:{}", 
            requestDto.getNpcID(), requestDto.getMapID(), requestDto.getGameType(), 
            requestDto.getTargetEmotion(), requestDto.getUserEmotion(), requestDto.isCorrect(),
            requestDto.getImageBase64() != null ? "존재" : "null", requestDto.getConfidence());
        
        log.info("게임 실행 요청: userIdentifier={}, gameType={}, mapID={}, npcID={}", 
            userIdentifier, requestDto.getGameType(), requestDto.getMapID(), requestDto.getNpcID());
        
        GameResponseDto response = gameService.processGame(userIdentifier, requestDto);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 게임 통계 조회 API
     */
    @GetMapping("/statistics")
    public ResponseEntity<GameStatisticsDto> getGameStatistics(@RequestHeader("Authorization") String tokenHeader) {
        String token = tokenHeader.replace("Bearer ", "");
        String userIdentifier = jwtUtil.extractUserIdentifier(token);
        
        log.info("게임 통계 조회 요청: userIdentifier={}", userIdentifier);
        
        GameStatisticsDto statistics = gameService.getGameStatistics(userIdentifier);
        
        if (statistics != null) {
            return ResponseEntity.ok(statistics);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 게임 기록 조회 API
     */
    @GetMapping("/history")
    public ResponseEntity<Map<String, Object>> getGameHistory(@RequestHeader("Authorization") String tokenHeader) {
        String token = tokenHeader.replace("Bearer ", "");
        String userIdentifier = jwtUtil.extractUserIdentifier(token);
        
        log.info("게임 기록 조회 요청: userIdentifier={}", userIdentifier);
        
        Map<String, Object> gameHistory = gameService.getGameHistory(userIdentifier);
        
        if (gameHistory != null) {
            return ResponseEntity.ok(gameHistory);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 특정 맵의 게임 기록 조회 API
     */
    @GetMapping("/history/map/{mapID}")
    public ResponseEntity<Map<String, Object>> getGameHistoryByMap(
            @RequestHeader("Authorization") String tokenHeader,
            @PathVariable String mapID) {
        String token = tokenHeader.replace("Bearer ", "");
        String userIdentifier = jwtUtil.extractUserIdentifier(token);
        
        log.info("맵별 게임 기록 조회 요청: userIdentifier={}, mapID={}", userIdentifier, mapID);
        
        Map<String, Object> gameHistory = gameService.getGameHistoryByMap(userIdentifier, mapID);
        
        if (gameHistory != null) {
            return ResponseEntity.ok(gameHistory);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 게임 상태 확인 API
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Game service is running");
    }
}
