package com.AMO.autismGame.Game.dto;

import com.AMO.autismGame.Game.Emotion;
import com.AMO.autismGame.Game.GameType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class GameRequestDto {
    @JsonProperty("npcID")
    private String npcID;
    
    @JsonProperty("mapID")
    private String mapID;
    
    @JsonProperty("gameType")
    private GameType gameType;
    
    @JsonProperty("targetEmotion")
    private Emotion targetEmotion;
    
    @JsonProperty("userEmotion")
    private Emotion userEmotion; // 사용자가 선택하거나 인식된 감정
    
    @JsonProperty("isCorrect")
    private boolean isCorrect = false; // 프론트엔드에서 판단한 정답 여부 (기본값 false)
    
    @JsonProperty("imageBase64")
    private String imageBase64; // 게임 2번용만 (EMOTION_CHOICE일 때는 null)
    
    @JsonProperty("confidence")
    private Double confidence; // 게임 2번용만 (EMOTION_CHOICE일 때는 null)
}
