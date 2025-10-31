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
@Table(name = "face_photo_game_sessions")
public class FacePhotoGameSession {
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
    private Emotion targetEmotion;

    @Enumerated(EnumType.STRING)
    private Emotion userEmotion;

    private boolean isCorrect;

    private String s3ImageKey;

    private Double confidence;

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
