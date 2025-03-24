package com.AMO.autismGame.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.List;

@Getter
@AllArgsConstructor
public class MapResponse {
    private String status;
    private List<MapDto> availableMaps;
}