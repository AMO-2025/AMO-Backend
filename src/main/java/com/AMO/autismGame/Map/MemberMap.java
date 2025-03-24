package com.AMO.autismGame.Map;

import com.AMO.autismGame.Member.Member;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class MemberMap {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id; // PK

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member; // 유저 정보 (N:1 관계)

    @ManyToOne
    @JoinColumn(name = "map_id", nullable = false)
    private MapEntity map; // 맵 정보 (N:1 관계)

    private boolean isUnlocked; // 해당 맵이 해금되었는지 여부
}