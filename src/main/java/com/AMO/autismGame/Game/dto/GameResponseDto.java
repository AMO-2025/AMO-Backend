package com.AMO.autismGame.Game.dto;

import com.AMO.autismGame.Game.Emotion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameResponseDto {
    private boolean success;
    private String message;
    private boolean isCorrect; // isCorrect로 통일
    private Emotion targetEmotion;
    private Emotion userEmotion;
    private String s3ImageKey; // 게임 2번용만 (EMOTION_CHOICE일 때는 null)
    private Double confidence; // 게임 2번용만 (EMOTION_CHOICE일 때는 null)
}
