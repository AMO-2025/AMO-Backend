package com.AMO.autismGame.Map;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class MapEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id; // PK


    @Column(unique = true, nullable = false)
    public String mapID;
    private String mapName; // 맵 이름 (예: "집", "공원", "식당")
}