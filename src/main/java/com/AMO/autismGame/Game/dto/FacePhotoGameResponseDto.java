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
public class FacePhotoGameResponseDto {
    private boolean success;
    private String message;
    private boolean isCorrect;
    private Emotion targetEmotion;
    private Emotion userEmotion;
    private String s3ImageKey;
    private Double confidence;
}
