package com.AMO.autismGame.Game;

import com.AMO.autismGame.Game.dto.GameRequestDto;
import com.AMO.autismGame.Game.dto.GameResponseDto;
import com.AMO.autismGame.Game.dto.GameStatisticsDto;
import com.AMO.autismGame.Member.Member;
import com.AMO.autismGame.Member.MemberRepository;
import com.AMO.autismGame.Npc.NpcEntity;
import com.AMO.autismGame.Npc.NpcRepository;
import com.AMO.autismGame.config.S3Config;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Optional;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GameService {

    private final GameSessionRepository gameSessionRepository;
    private final MemberRepository memberRepository;
    private final NpcRepository npcRepository;
    private final AmazonS3Client amazonS3Client;
    private final S3Config s3Config;

    @Transactional
    public GameResponseDto processGame(String userIdentifier, GameRequestDto requestDto) {
        try {
            // 회원 확인
            Optional<Member> memberOpt = memberRepository.findByUserIdentifier(userIdentifier);
            if (memberOpt.isEmpty()) {
                return createErrorResponse("회원을 찾을 수 없습니다.");
            }
            Member member = memberOpt.get();

            // NPC 확인
            Optional<NpcEntity> npcOpt = npcRepository.findByMapIDAndNpcID(requestDto.getMapID(), requestDto.getNpcID());
            if (npcOpt.isEmpty()) {
                return createErrorResponse("NPC를 찾을 수 없습니다.");
            }
            NpcEntity npc = npcOpt.get();

            // 게임 타입에 따른 처리
            if (requestDto.getGameType() == GameType.EMOTION_CHOICE) {
                return processEmotionChoiceGame(member, npc, requestDto);
            } else if (requestDto.getGameType() == GameType.FACE_PHOTO) {
                return processFacePhotoGame(member, npc, requestDto);
            } else {
                return createErrorResponse("지원하지 않는 게임 타입입니다.");
            }

        } catch (Exception e) {
            log.error("게임 처리 중 오류 발생: ", e);
            return createErrorResponse("게임 처리 중 오류가 발생했습니다.");
        }
    }

    private GameResponseDto processEmotionChoiceGame(Member member, NpcEntity npc, GameRequestDto requestDto) {
        // 게임 1번: 감정 선택 게임
        // 프론트엔드에서 전달받은 정답 여부 사용
        boolean isCorrect = requestDto.isCorrect();

        // 게임 세션 저장
        GameSession gameSession = new GameSession();
        gameSession.setMember(member);
        gameSession.setNpc(npc);
        gameSession.setGameType(GameType.EMOTION_CHOICE);
        gameSession.setTargetEmotion(requestDto.getTargetEmotion());
        gameSession.setUserEmotion(requestDto.getUserEmotion());
        gameSession.setCorrect(isCorrect);

        try {
            GameSession savedSession = gameSessionRepository.save(gameSession);
            log.info("감정 선택 게임 세션 저장 완료: sessionId={}, memberId={}, npcId={}, mapId={}, gameType={}, targetEmotion={}, userEmotion={}, isCorrect={}", 
                savedSession.getId(), 
                member.getId(), 
                npc.getNpcID(), 
                npc.getMapID(), 
                GameType.EMOTION_CHOICE, 
                requestDto.getTargetEmotion(), 
                requestDto.getUserEmotion(), 
                isCorrect);
        } catch (Exception e) {
            log.error("게임 세션 저장 실패: memberId={}, npcId={}, mapId={}", member.getId(), npc.getNpcID(), npc.getMapID(), e);
            return createErrorResponse("게임 세션 저장에 실패했습니다.");
        }

        // 응답 생성
        GameResponseDto response = GameResponseDto.builder()
            .success(true)
            .message("감정 선택 게임이 완료되었습니다.")
            .isCorrect(isCorrect)
            .targetEmotion(requestDto.getTargetEmotion())
            .userEmotion(requestDto.getUserEmotion())
            .build();

        return response;
    }

    private GameResponseDto processFacePhotoGame(Member member, NpcEntity npc, GameRequestDto requestDto) {
        try {
            // 게임 2번: 얼굴 촬영 게임
            if (requestDto.getImageBase64() == null || requestDto.getImageBase64().isEmpty()) {
                return createErrorResponse("이미지가 제공되지 않았습니다.");
            }

            // Base64 이미지 유효성 검증
            if (!isValidBase64Image(requestDto.getImageBase64())) {
                log.warn("잘못된 Base64 이미지 형식: userIdentifier={}, npcId={}, mapId={}", 
                    member.getUserIdentifier(), npc.getNpcID(), npc.getMapID());
                return createErrorResponse("잘못된 이미지 형식입니다. Base64 인코딩을 확인해주세요.");
            }

            // 정답 여부 확인 (디버깅 로그 추가)
            boolean isCorrect = requestDto.isCorrect();
            log.info("🔍 DEBUG: requestDto.isCorrect() 원본 값 = {}", requestDto.isCorrect());
            log.info("🔍 DEBUG: processFacePhotoGame 내 isCorrect 변수 값 = {}", isCorrect);
            log.info("🔍 DEBUG: targetEmotion = {}, userEmotion = {}", requestDto.getTargetEmotion(), requestDto.getUserEmotion());

            // Base64 이미지를 S3에 업로드
            String s3ImageKey = uploadImageToS3(requestDto.getImageBase64(), member, npc, requestDto.getTargetEmotion(), isCorrect);
            log.info("🔍 DEBUG: S3 이미지 키 생성 시 사용된 isCorrect 값 = {}", isCorrect);
            
            // 프론트엔드에서 전달받은 감정 분석 결과와 정답 여부 사용
            Emotion detectedEmotion = requestDto.getUserEmotion();

            // 게임 세션 저장
            GameSession gameSession = new GameSession();
            gameSession.setMember(member);
            gameSession.setNpc(npc);
            gameSession.setGameType(GameType.FACE_PHOTO);
            gameSession.setTargetEmotion(requestDto.getTargetEmotion());
            gameSession.setUserEmotion(detectedEmotion);
            gameSession.setCorrect(isCorrect);
            gameSession.setS3ImageKey(s3ImageKey);

            try {
                GameSession savedSession = gameSessionRepository.save(gameSession);
                log.info("얼굴 촬영 게임 세션 저장 완료: sessionId={}, memberId={}, npcId={}, mapId={}, gameType={}, targetEmotion={}, userEmotion={}, isCorrect={}, s3ImageKey={}", 
                    savedSession.getId(), 
                    member.getId(), 
                    npc.getNpcID(), 
                    npc.getMapID(), 
                    GameType.FACE_PHOTO, 
                    requestDto.getTargetEmotion(), 
                    detectedEmotion, 
                    isCorrect,
                    s3ImageKey);
            } catch (Exception e) {
                log.error("게임 세션 저장 실패: memberId={}, npcId={}, mapId={}", member.getId(), npc.getNpcID(), npc.getMapID(), e);
                return createErrorResponse("게임 세션 저장에 실패했습니다.");
            }

            // 응답 생성
            GameResponseDto response = GameResponseDto.builder()
                .success(true)
                .message("얼굴 촬영 게임이 완료되었습니다.")
                .isCorrect(isCorrect)
                .targetEmotion(requestDto.getTargetEmotion())
                .userEmotion(detectedEmotion)
                .s3ImageKey(s3ImageKey)
                .confidence(requestDto.getConfidence())
                .build();

            log.info("🔍 DEBUG: 최종 응답에 설정된 correct 값 = {}", response.isCorrect());

            return response;

        } catch (Exception e) {
            log.error("얼굴 촬영 게임 처리 중 오류: ", e);
            return createErrorResponse("얼굴 촬영 게임 처리 중 오류가 발생했습니다.");
        }
    }

    private String uploadImageToS3(String base64Image, Member member, NpcEntity npc, Emotion targetEmotion, boolean isCorrect) {
        try {
            // Base64 문자열 검증 및 정리
            if (base64Image == null || base64Image.trim().isEmpty()) {
                throw new IllegalArgumentException("Base64 이미지 문자열이 비어있습니다.");
            }

            log.info("Base64 이미지 처리 시작: 원본 길이={}, memberId={}, npcId={}, mapId={}", 
                base64Image.length(), member.getId(), npc.getNpcID(), npc.getMapID());

            // Base64 문자열에서 실제 데이터 부분 추출
            String base64Data;
            if (base64Image.contains(",")) {
                // "data:image/jpeg;base64,실제데이터" 형식인 경우
                String[] parts = base64Image.split(",", 2); // 최대 2개로만 분할
                if (parts.length < 2) {
                    log.warn("잘못된 Base64 형식: 콤마가 있지만 데이터가 없음. 원본: {}", base64Image.substring(0, Math.min(100, base64Image.length())));
                    throw new IllegalArgumentException("잘못된 Base64 형식입니다: 콤마가 있지만 데이터가 없습니다.");
                }
                base64Data = parts[1];
                log.info("Data URL 형식 감지: 헤더 길이={}, 데이터 길이={}", parts[0].length(), base64Data.length());
            } else {
                // 순수 Base64 문자열인 경우
                base64Data = base64Image;
                log.info("순수 Base64 형식 감지: 데이터 길이={}", base64Data.length());
            }

            // Base64 문자열 정리 (공백, 줄바꿈, 탭 제거)
            base64Data = base64Data.replaceAll("\\s", "");
            
            // Base64 문자열 길이 검증 (4의 배수여야 함)
            if (base64Data.length() % 4 != 0) {
                log.warn("Base64 문자열 길이가 4의 배수가 아님: 길이={}", base64Data.length());
                // 패딩 추가 시도
                int padding = 4 - (base64Data.length() % 4);
                if (padding < 4) {
                    base64Data = base64Data + "=".repeat(padding);
                    log.info("Base64 패딩 추가: 원본 길이={}, 패딩 후 길이={}", base64Data.length() - padding, base64Data.length());
                }
            }

            // Base64 문자셋 검증
            if (!base64Data.matches("^[A-Za-z0-9+/]*={0,2}$")) {
                log.warn("Base64 문자셋 검증 실패: 잘못된 문자 포함");
                // 특수 문자 제거 시도
                base64Data = base64Data.replaceAll("[^A-Za-z0-9+/=]", "");
                log.info("특수 문자 제거 후 길이: {}", base64Data.length());
            }

            // Base64 디코딩
            byte[] imageBytes;
            try {
                imageBytes = Base64.getDecoder().decode(base64Data);
                log.info("Base64 디코딩 성공: 원본 길이={}, 디코딩된 바이트={}", base64Data.length(), imageBytes.length);
            } catch (IllegalArgumentException e) {
                log.error("Base64 디코딩 실패: {}", e.getMessage());
                log.error("Base64 데이터 샘플: {}", base64Data.substring(0, Math.min(100, base64Data.length())));
                
                // 더 관대한 디코딩 시도
                try {
                    // MIME 디코더 사용
                    imageBytes = Base64.getMimeDecoder().decode(base64Data);
                    log.info("MIME 디코더로 디코딩 성공: 바이트={}", imageBytes.length);
                } catch (Exception e2) {
                    log.error("MIME 디코더도 실패: {}", e2.getMessage());
                    throw new IllegalArgumentException("Base64 디코딩에 실패했습니다: " + e.getMessage());
                }
            }

            if (imageBytes.length == 0) {
                throw new IllegalArgumentException("디코딩된 이미지 데이터가 비어있습니다.");
            }

            // 이미지 파일 형식 검증 (JPEG, PNG 등)
            if (!isValidImageFormat(imageBytes)) {
                log.warn("잘못된 이미지 형식: 바이트 길이={}", imageBytes.length);
                throw new IllegalArgumentException("지원하지 않는 이미지 형식입니다.");
            }
            
            // S3 키 생성: 정답일 때는 감정 없이, 오답일 때는 감정별로 세분화
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String s3Key;
            
            if (isCorrect) {
                // 정답일 때: memberId/mapId/npcId/correct/timestamp.jpg
                s3Key = String.format("game-images/%d/%s/%s/correct/%s.jpg",
                    member.getId(),
                    npc.getMapID(),
                    npc.getNpcID(),
                    timestamp
                );
            } else {
                // 오답일 때: memberId/mapId/npcId/incorrect/emotion/timestamp.jpg
                s3Key = String.format("game-images/%d/%s/%s/incorrect/%s/%s.jpg",
                    member.getId(),
                    npc.getMapID(),
                    npc.getNpcID(),
                    targetEmotion.name(),
                    timestamp
                );
            }

            // S3에 업로드
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType("image/jpeg");
            metadata.setContentLength(imageBytes.length);

            amazonS3Client.putObject(s3Config.getBucketName(), s3Key, 
                new java.io.ByteArrayInputStream(imageBytes), metadata);
            
            log.info("이미지가 S3에 업로드되었습니다: {}", s3Key);
            return s3Key;

        } catch (Exception e) {
            log.error("S3 업로드 중 오류: base64Length={}, memberId={}, npcId={}, mapId={}", 
                base64Image != null ? base64Image.length() : 0, 
                member.getId(), 
                npc.getNpcID(), 
                npc.getMapID(), e);
            throw new RuntimeException("이미지 업로드에 실패했습니다: " + e.getMessage(), e);
        }
    }

    // 이미지 파일 형식 검증 헬퍼 메서드
    private boolean isValidImageFormat(byte[] imageBytes) {
        if (imageBytes.length < 4) {
            return false;
        }
        
        // JPEG 시그니처 확인
        if (imageBytes[0] == (byte) 0xFF && imageBytes[1] == (byte) 0xD8 && 
            imageBytes[2] == (byte) 0xFF) {
            return true;
        }
        
        // PNG 시그니처 확인
        if (imageBytes[0] == (byte) 0x89 && imageBytes[1] == (byte) 0x50 && 
            imageBytes[2] == (byte) 0x4E && imageBytes[3] == (byte) 0x47) {
            return true;
        }
        
        // GIF 시그니처 확인
        if ((imageBytes[0] == (byte) 0x47 && imageBytes[1] == (byte) 0x49 && 
             imageBytes[2] == (byte) 0x46) ||
            (imageBytes[0] == (byte) 0x47 && imageBytes[1] == (byte) 0x49 && 
             imageBytes[2] == (byte) 0x46)) {
            return true;
        }
        
        return false;
    }


    private GameResponseDto createErrorResponse(String message) {
        GameResponseDto response = new GameResponseDto();
        response.setSuccess(false);
        response.setMessage(message);
        return response;
    }

    // 게임 통계 조회
    public GameStatisticsDto getGameStatistics(String userIdentifier) {
        Optional<Member> memberOpt = memberRepository.findByUserIdentifier(userIdentifier);
        if (memberOpt.isEmpty()) {
            return null;
        }

        Member member = memberOpt.get();
        long totalGames = gameSessionRepository.countTotalGamesByMember(member);
        long correctAnswers = gameSessionRepository.countCorrectAnswersByMember(member);
        
        double accuracy = totalGames > 0 ? (double) correctAnswers / totalGames * 100 : 0;

        return GameStatisticsDto.builder()
            .totalGames(totalGames)
            .correctAnswers(correctAnswers)
            .accuracy(accuracy)
            .build();
    }

    // 게임 기록 조회
    public Map<String, Object> getGameHistory(String userIdentifier) {
        Optional<Member> memberOpt = memberRepository.findByUserIdentifier(userIdentifier);
        if (memberOpt.isEmpty()) {
            return null;
        }

        Member member = memberOpt.get();
        List<GameSession> gameSessions = gameSessionRepository.findByMember(member);
        
        Map<String, Object> history = new HashMap<>();
        history.put("userIdentifier", userIdentifier);
        history.put("totalGames", gameSessions.size());
        
        // 게임 타입별 통계
        Map<String, Long> gameTypeStats = gameSessions.stream()
            .collect(Collectors.groupingBy(
                gs -> gs.getGameType().name(),
                Collectors.counting()
            ));
        history.put("gameTypeStats", gameTypeStats);
        
        // 맵별 통계
        Map<String, Long> mapStats = gameSessions.stream()
            .collect(Collectors.groupingBy(
                gs -> gs.getNpc().getMapID(),
                Collectors.counting()
            ));
        history.put("mapStats", mapStats);
        
        // NPC별 통계
        Map<String, Long> npcStats = gameSessions.stream()
            .collect(Collectors.groupingBy(
                gs -> gs.getNpc().getNpcID(),
                Collectors.counting()
            ));
        history.put("npcStats", npcStats);
        
        // 최근 게임 기록 (최대 10개)
        List<Map<String, Object>> recentGames = gameSessions.stream()
            .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
            .limit(10)
            .map(this::convertGameSessionToMap)
            .collect(Collectors.toList());
        history.put("recentGames", recentGames);
        
        return history;
    }

    // 특정 맵의 게임 기록 조회
    public Map<String, Object> getGameHistoryByMap(String userIdentifier, String mapID) {
        Optional<Member> memberOpt = memberRepository.findByUserIdentifier(userIdentifier);
        if (memberOpt.isEmpty()) {
            return null;
        }

        Member member = memberOpt.get();
        List<GameSession> gameSessions = gameSessionRepository.findByMapID(mapID);
        
        // 해당 맵에서 이 사용자가 플레이한 게임만 필터링
        List<GameSession> userMapGames = gameSessions.stream()
            .filter(gs -> gs.getMember().getId() == member.getId())
            .collect(Collectors.toList());
        
        Map<String, Object> mapHistory = new HashMap<>();
        mapHistory.put("userIdentifier", userIdentifier);
        mapHistory.put("mapID", mapID);
        mapHistory.put("totalGames", userMapGames.size());
        
        // 정답률 계산
        long correctAnswers = userMapGames.stream()
            .filter(GameSession::isCorrect)
            .count();
        double accuracy = userMapGames.size() > 0 ? (double) correctAnswers / userMapGames.size() * 100 : 0;
        mapHistory.put("correctAnswers", correctAnswers);
        mapHistory.put("accuracy", accuracy);
        
        // 게임 타입별 통계
        Map<String, Long> gameTypeStats = userMapGames.stream()
            .collect(Collectors.groupingBy(
                gs -> gs.getGameType().name(),
                Collectors.counting()
            ));
        mapHistory.put("gameTypeStats", gameTypeStats);
        
        // NPC별 통계
        Map<String, Long> npcStats = userMapGames.stream()
            .collect(Collectors.groupingBy(
                gs -> gs.getNpc().getNpcID(),
                Collectors.counting()
            ));
        mapHistory.put("npcStats", npcStats);
        
        // 상세 게임 기록
        List<Map<String, Object>> detailedGames = userMapGames.stream()
            .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
            .map(this::convertGameSessionToMap)
            .collect(Collectors.toList());
        mapHistory.put("detailedGames", detailedGames);
        
        return mapHistory;
    }

    // GameSession을 Map으로 변환하는 헬퍼 메서드
    private Map<String, Object> convertGameSessionToMap(GameSession gameSession) {
        Map<String, Object> gameMap = new HashMap<>();
        gameMap.put("sessionId", gameSession.getId());
        gameMap.put("gameType", gameSession.getGameType().name());
        gameMap.put("targetEmotion", gameSession.getTargetEmotion().name());
        gameMap.put("userEmotion", gameSession.getUserEmotion() != null ? gameSession.getUserEmotion().name() : null);
        gameMap.put("isCorrect", gameSession.isCorrect());
        gameMap.put("npcID", gameSession.getNpc().getNpcID());
        gameMap.put("mapID", gameSession.getNpc().getMapID());
        gameMap.put("createdAt", gameSession.getCreatedAt());
        gameMap.put("s3ImageKey", gameSession.getS3ImageKey());
        return gameMap;
    }

    // Base64 이미지 유효성 검증 헬퍼 메서드
    private boolean isValidBase64Image(String base64Image) {
        if (base64Image == null || base64Image.trim().isEmpty()) {
            return false;
        }

        try {
            // Base64 문자열에서 실제 데이터 부분 추출
            String base64Data;
            if (base64Image.contains(",")) {
                String[] parts = base64Image.split(",", 2);
                if (parts.length < 2) {
                    return false;
                }
                base64Data = parts[1];
            } else {
                base64Data = base64Image;
            }

            // 공백 제거
            base64Data = base64Data.replaceAll("\\s", "");

            // Base64 문자열 길이 검증 (4의 배수여야 함)
            if (base64Data.length() % 4 != 0) {
                return false;
            }

            // Base64 문자셋 검증
            if (!base64Data.matches("^[A-Za-z0-9+/]*={0,2}$")) {
                return false;
            }

            // 실제 디코딩 시도 (가장 확실한 검증)
            try {
                byte[] testBytes = Base64.getDecoder().decode(base64Data);
                return testBytes.length > 0;
            } catch (IllegalArgumentException e) {
                // MIME 디코더로 재시도
                try {
                    byte[] testBytes = Base64.getMimeDecoder().decode(base64Data);
                    return testBytes.length > 0;
                } catch (Exception e2) {
                    return false;
                }
            }
        } catch (Exception e) {
            return false;
        }
    }
}
