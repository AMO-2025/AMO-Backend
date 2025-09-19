package com.AMO.autismGame.Game.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class GameStatisticsDto {
    private long totalGames;
    private long correctAnswers;
    private double accuracy; // 정확도 (퍼센트)
}
