package com.AMO.autismGame.Npc;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface NpcRepository extends JpaRepository<NpcEntity, Integer> {
    Optional<NpcEntity> findByMapIDAndNpcID(String mapID, String npcID);
} 