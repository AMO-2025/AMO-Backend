package com.AMO.autismGame.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MapDto {
    private int mapID;
    private String mapName;
    private boolean isUnlocked;
}
