package com.AMO.autismGame.Game.dto;

import com.AMO.autismGame.Game.Emotion;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class EmotionChoiceGameRequestDto {
    @JsonProperty("npcID")
    private String npcID;

    @JsonProperty("mapID")
    private String mapID;

    @JsonProperty("targetEmotion")
    private Emotion targetEmotion;

    @JsonProperty("userEmotion")
    private Emotion userEmotion; // 사용자가 선택하거나 인식된 감정

    @JsonProperty("isCorrect")
    private boolean isCorrect = false; // 프론트엔드에서 판단한 정답 여부 (기본값 false)
}
