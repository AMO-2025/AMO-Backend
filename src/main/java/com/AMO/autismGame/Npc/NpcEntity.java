package com.AMO.autismGame.Npc;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class NpcEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private String npcID;

    @Column(nullable = false)
    private String mapID;

    private String npcName;
} 