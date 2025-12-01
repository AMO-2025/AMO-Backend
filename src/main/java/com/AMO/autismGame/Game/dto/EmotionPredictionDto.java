package com.AMO.autismGame.Game.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmotionPredictionDto {
    private boolean success;
    private String message;
    private String predictedEmotion;
    private double confidence;
}
