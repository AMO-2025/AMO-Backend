package com.AMO.autismGame.Game.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmotionStatistic {
    private long correct;
    private long total;
    private double accuracy;
}
