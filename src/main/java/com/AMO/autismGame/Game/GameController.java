package com.AMO.autismGame.Game;

import com.AMO.autismGame.Game.dto.*;
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

    @PostMapping("/play/emotion-choice")
    public ResponseEntity<EmotionChoiceGameResponseDto> playEmotionChoiceGame(
            @RequestHeader("Authorization") String tokenHeader,
            @RequestBody EmotionChoiceGameRequestDto requestDto) {
        String token = tokenHeader.replace("Bearer ", "");
        String userIdentifier = jwtUtil.extractUserIdentifier(token);
        EmotionChoiceGameResponseDto response = gameService.processEmotionChoiceGame(userIdentifier, requestDto);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/play/face-photo")
    public ResponseEntity<FacePhotoGameResponseDto> playFacePhotoGame(
            @RequestHeader("Authorization") String tokenHeader,
            @RequestBody FacePhotoGameRequestDto requestDto) {
        String token = tokenHeader.replace("Bearer ", "");
        String userIdentifier = jwtUtil.extractUserIdentifier(token);
        FacePhotoGameResponseDto response = gameService.processFacePhotoGame(userIdentifier, requestDto);
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
