package com.AMO.autismGame.Game;

import com.AMO.autismGame.Member.Member;
import com.AMO.autismGame.Npc.NpcEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "game_sessions")
public class GameSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne
    @JoinColumn(name = "npc_id", nullable = false)
    private NpcEntity npc;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GameType gameType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Emotion targetEmotion; // NPC가 요구하는 감정

    @Enumerated(EnumType.STRING)
    private Emotion userEmotion; // 사용자가 선택하거나 인식된 감정

    private boolean isCorrect; // 정답 여부

    private String s3ImageKey; // S3에 저장된 이미지 키 (게임 2번용만)

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
