package com.AMO.autismGame.Game.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@Builder
public class DetailedGameStatisticsDto {
    private GameTypeStatistics facePhotoGame;
    private GameTypeStatistics emotionChoiceGame;
}
