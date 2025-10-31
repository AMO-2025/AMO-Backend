package com.AMO.autismGame.Game.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@Builder
public class GameTypeStatistics {
    private long totalGames;
    private long correctAnswers;
    private double overallAccuracy;
    private Map<String, EmotionStatistic> emotionStatistics;
}
