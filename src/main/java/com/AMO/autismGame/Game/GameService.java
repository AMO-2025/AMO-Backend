package com.AMO.autismGame.Game;

import com.AMO.autismGame.Game.dto.*;
import com.AMO.autismGame.Member.Member;
import com.AMO.autismGame.Member.MemberRepository;
import com.AMO.autismGame.Npc.NpcEntity;
import com.AMO.autismGame.Npc.NpcRepository;
import com.AMO.autismGame.config.S3Config;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GameService {

    private final EmotionChoiceGameSessionRepository emotionChoiceGameSessionRepository;
    private final FacePhotoGameSessionRepository facePhotoGameSessionRepository;
    private final MemberRepository memberRepository;
    private final NpcRepository npcRepository;
    private final S3Client s3Client;
    private final S3Config s3Config;

    @Transactional
    public EmotionChoiceGameResponseDto processEmotionChoiceGame(String userIdentifier, EmotionChoiceGameRequestDto requestDto) {
        Optional<Member> memberOpt = memberRepository.findByUserIdentifier(userIdentifier);
        if (memberOpt.isEmpty()) {
            return createEmotionChoiceErrorResponse("회원을 찾을 수 없습니다.");
        }
        Member member = memberOpt.get();

        Optional<NpcEntity> npcOpt = npcRepository.findByMapIDAndNpcID(requestDto.getMapID(), requestDto.getNpcID());
        if (npcOpt.isEmpty()) {
            return createEmotionChoiceErrorResponse("NPC를 찾을 수 없습니다.");
        }
        NpcEntity npc = npcOpt.get();

        boolean isCorrect = requestDto.isCorrect();

        EmotionChoiceGameSession gameSession = new EmotionChoiceGameSession();
        gameSession.setMember(member);
        gameSession.setNpc(npc);
        gameSession.setTargetEmotion(requestDto.getTargetEmotion());
        gameSession.setUserEmotion(requestDto.getUserEmotion());
        gameSession.setCorrect(isCorrect);

        try {
            emotionChoiceGameSessionRepository.save(gameSession);
        } catch (Exception e) {
            log.error("게임 세션 저장 실패: memberId={}, npcId={}, mapId={}", member.getId(), npc.getNpcID(), npc.getMapID(), e);
            return createEmotionChoiceErrorResponse("게임 세션 저장에 실패했습니다.");
        }

        return EmotionChoiceGameResponseDto.builder()
            .success(true)
            .message("감정 선택 게임이 완료되었습니다.")
            .isCorrect(isCorrect)
            .targetEmotion(requestDto.getTargetEmotion())
            .userEmotion(requestDto.getUserEmotion())
            .build();
    }

    @Transactional
    public FacePhotoGameResponseDto processFacePhotoGame(String userIdentifier, FacePhotoGameRequestDto requestDto) {
        try {
            Optional<Member> memberOpt = memberRepository.findByUserIdentifier(userIdentifier);
            if (memberOpt.isEmpty()) {
                return createFacePhotoErrorResponse("회원을 찾을 수 없습니다.");
            }
            Member member = memberOpt.get();

            Optional<NpcEntity> npcOpt = npcRepository.findByMapIDAndNpcID(requestDto.getMapID(), requestDto.getNpcID());
            if (npcOpt.isEmpty()) {
                return createFacePhotoErrorResponse("NPC를 찾을 수 없습니다.");
            }
            NpcEntity npc = npcOpt.get();

            if (requestDto.getImageBase64() == null || requestDto.getImageBase64().isEmpty()) {
                return createFacePhotoErrorResponse("이미지가 제공되지 않았습니다.");
            }

            if (!isValidBase64Image(requestDto.getImageBase64())) {
                log.warn("잘못된 Base64 이미지 형식: userIdentifier={}, npcId={}, mapId={}",
                    member.getUserIdentifier(), npc.getNpcID(), npc.getMapID());
                return createFacePhotoErrorResponse("잘못된 이미지 형식입니다. Base64 인코딩을 확인해주세요.");
            }

            boolean isCorrect = requestDto.isCorrect();
            String s3ImageKey = uploadImageToS3(requestDto.getImageBase64(), member, npc, requestDto.getTargetEmotion(), isCorrect);

            FacePhotoGameSession gameSession = new FacePhotoGameSession();
            gameSession.setMember(member);
            gameSession.setNpc(npc);
            gameSession.setTargetEmotion(requestDto.getTargetEmotion());
            gameSession.setUserEmotion(requestDto.getUserEmotion());
            gameSession.setCorrect(isCorrect);
            gameSession.setS3ImageKey(s3ImageKey);
            gameSession.setConfidence(requestDto.getConfidence());

            try {
                facePhotoGameSessionRepository.save(gameSession);
            } catch (Exception e) {
                log.error("게임 세션 저장 실패: memberId={}, npcId={}, mapId={}", member.getId(), npc.getNpcID(), npc.getMapID(), e);
                return createFacePhotoErrorResponse("게임 세션 저장에 실패했습니다.");
            }

            return FacePhotoGameResponseDto.builder()
                .success(true)
                .message("얼굴 촬영 게임이 완료되었습니다.")
                .isCorrect(isCorrect)
                .targetEmotion(requestDto.getTargetEmotion())
                .userEmotion(requestDto.getUserEmotion())
                .s3ImageKey(s3ImageKey)
                .confidence(requestDto.getConfidence())
                .build();

        } catch (Exception e) {
            log.error("얼굴 촬영 게임 처리 중 오류: ", e);
            return createFacePhotoErrorResponse("얼굴 촬영 게임 처리 중 오류가 발생했습니다.");
        }
    }

    private String uploadImageToS3(String base64Image, Member member, NpcEntity npc, Emotion targetEmotion, boolean isCorrect) {
        try {
            if (base64Image == null || base64Image.trim().isEmpty()) {
                throw new IllegalArgumentException("Base64 이미지 문자열이 비어있습니다.");
            }

            String base64Data = base64Image.contains(",") ? base64Image.split(",")[1] : base64Image;
            byte[] imageBytes = Base64.getDecoder().decode(base64Data);

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String s3Key;

            if (isCorrect) {
                s3Key = String.format("game-images/%d/%s/%s/correct/%s.jpg",
                    member.getId(),
                    npc.getMapID(),
                    npc.getNpcID(),
                    timestamp
                );
            } else {
                s3Key = String.format("game-images/%d/%s/%s/incorrect/%s/%s.jpg",
                    member.getId(),
                    npc.getMapID(),
                    npc.getNpcID(),
                    targetEmotion.name(),
                    timestamp
                );
            }

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(s3Config.getBucketName())
                .key(s3Key)
                .contentType("image/jpeg")
                .contentLength((long) imageBytes.length)
                .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(imageBytes));

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

    private boolean isValidBase64Image(String base64Image) {
        if (base64Image == null || base64Image.trim().isEmpty()) {
            return false;
        }
        try {
            String base64Data = base64Image.contains(",") ? base64Image.split(",")[1] : base64Image;
            Base64.getDecoder().decode(base64Data);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private EmotionChoiceGameResponseDto createEmotionChoiceErrorResponse(String message) {
        return EmotionChoiceGameResponseDto.builder().success(false).message(message).build();
    }

    private FacePhotoGameResponseDto createFacePhotoErrorResponse(String message) {
        return FacePhotoGameResponseDto.builder().success(false).message(message).build();
    }

    public GameStatisticsDto getGameStatistics(String userIdentifier) {
        Optional<Member> memberOpt = memberRepository.findByUserIdentifier(userIdentifier);
        if (memberOpt.isEmpty()) {
            return null;
        }
        Member member = memberOpt.get();

        long totalGames = emotionChoiceGameSessionRepository.countByMember(member) + facePhotoGameSessionRepository.countByMember(member);
        long correctAnswers = emotionChoiceGameSessionRepository.countByMemberAndIsCorrect(member, true) + facePhotoGameSessionRepository.countByMemberAndIsCorrect(member, true);

        double accuracy = totalGames > 0 ? (double) correctAnswers / totalGames * 100 : 0;

        return GameStatisticsDto.builder()
            .totalGames(totalGames)
            .correctAnswers(correctAnswers)
            .accuracy(accuracy)
            .build();
    }

    public Map<String, Object> getGameHistory(String userIdentifier) {
        Optional<Member> memberOpt = memberRepository.findByUserIdentifier(userIdentifier);
        if (memberOpt.isEmpty()) {
            return null;
        }
        Member member = memberOpt.get();

        List<Map<String, Object>> gameHistory = new ArrayList<>();

        List<EmotionChoiceGameSession> emotionChoiceGames = emotionChoiceGameSessionRepository.findByMember(member);
        for (EmotionChoiceGameSession session : emotionChoiceGames) {
            gameHistory.add(convertEmotionChoiceGameSessionToMap(session));
        }

        List<FacePhotoGameSession> facePhotoGames = facePhotoGameSessionRepository.findByMember(member);
        for (FacePhotoGameSession session : facePhotoGames) {
            gameHistory.add(convertFacePhotoGameSessionToMap(session));
        }

        gameHistory.sort(Comparator.comparing((Map<String, Object> m) -> (LocalDateTime) m.get("createdAt")).reversed());

        Map<String, Object> result = new HashMap<>();
        result.put("userIdentifier", userIdentifier);
        result.put("totalGames", gameHistory.size());

        Map<String, Long> gameTypeStats = gameHistory.stream()
            .collect(Collectors.groupingBy(m -> (String) m.get("gameType"), Collectors.counting()));
        result.put("gameTypeStats", gameTypeStats);

        Map<String, Long> mapStats = gameHistory.stream()
            .collect(Collectors.groupingBy(m -> (String) m.get("mapID"), Collectors.counting()));
        result.put("mapStats", mapStats);

        Map<String, Long> npcStats = gameHistory.stream()
            .collect(Collectors.groupingBy(m -> (String) m.get("npcID"), Collectors.counting()));
        result.put("npcStats", npcStats);

        List<Map<String, Object>> recentGames = gameHistory.stream()
            .limit(10)
            .collect(Collectors.toList());
        result.put("recentGames", recentGames);

        return result;
    }

    public Map<String, Object> getGameHistoryByMap(String userIdentifier, String mapID) {
        Optional<Member> memberOpt = memberRepository.findByUserIdentifier(userIdentifier);
        if (memberOpt.isEmpty()) {
            return null;
        }
        Member member = memberOpt.get();

        List<Map<String, Object>> gameHistory = new ArrayList<>();

        List<EmotionChoiceGameSession> emotionChoiceGames = emotionChoiceGameSessionRepository.findByMemberAndNpc_MapID(member, mapID);
        for (EmotionChoiceGameSession session : emotionChoiceGames) {
            gameHistory.add(convertEmotionChoiceGameSessionToMap(session));
        }

        List<FacePhotoGameSession> facePhotoGames = facePhotoGameSessionRepository.findByMemberAndNpc_MapID(member, mapID);
        for (FacePhotoGameSession session : facePhotoGames) {
            gameHistory.add(convertFacePhotoGameSessionToMap(session));
        }

        gameHistory.sort(Comparator.comparing((Map<String, Object> m) -> (LocalDateTime) m.get("createdAt")).reversed());

        Map<String, Object> mapHistoryResult = new HashMap<>();
        mapHistoryResult.put("userIdentifier", userIdentifier);
        mapHistoryResult.put("mapID", mapID);
        mapHistoryResult.put("totalGames", gameHistory.size());

        long correctAnswers = gameHistory.stream()
            .filter(m -> (boolean) m.get("isCorrect"))
            .count();
        double accuracy = gameHistory.size() > 0 ? (double) correctAnswers / gameHistory.size() * 100 : 0;
        mapHistoryResult.put("correctAnswers", correctAnswers);
        mapHistoryResult.put("accuracy", accuracy);

        Map<String, Long> gameTypeStats = gameHistory.stream()
            .collect(Collectors.groupingBy(m -> (String) m.get("gameType"), Collectors.counting()));
        mapHistoryResult.put("gameTypeStats", gameTypeStats);

        Map<String, Long> npcStats = gameHistory.stream()
            .collect(Collectors.groupingBy(m -> (String) m.get("npcID"), Collectors.counting()));
        mapHistoryResult.put("npcStats", npcStats);

        mapHistoryResult.put("detailedGames", gameHistory);

        return mapHistoryResult;
    }

    private Map<String, Object> convertEmotionChoiceGameSessionToMap(EmotionChoiceGameSession session) {
        Map<String, Object> map = new HashMap<>();
        map.put("sessionId", session.getId());
        map.put("gameType", "EMOTION_CHOICE");
        map.put("targetEmotion", session.getTargetEmotion().name());
        map.put("userEmotion", session.getUserEmotion() != null ? session.getUserEmotion().name() : null);
        map.put("isCorrect", session.isCorrect());
        map.put("npcID", session.getNpc().getNpcID());
        map.put("mapID", session.getNpc().getMapID());
        map.put("createdAt", session.getCreatedAt());
        return map;
    }

    private Map<String, Object> convertFacePhotoGameSessionToMap(FacePhotoGameSession session) {
        Map<String, Object> map = new HashMap<>();
        map.put("sessionId", session.getId());
        map.put("gameType", "FACE_PHOTO");
        map.put("targetEmotion", session.getTargetEmotion().name());
        map.put("userEmotion", session.getUserEmotion() != null ? session.getUserEmotion().name() : null);
        map.put("isCorrect", session.isCorrect());
        map.put("npcID", session.getNpc().getNpcID());
        map.put("mapID", session.getNpc().getMapID());
        map.put("createdAt", session.getCreatedAt());
        map.put("s3ImageKey", session.getS3ImageKey());
        map.put("confidence", session.getConfidence());
        return map;
    }
}
