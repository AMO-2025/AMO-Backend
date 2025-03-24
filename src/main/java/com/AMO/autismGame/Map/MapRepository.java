package com.AMO.autismGame.Map;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MapRepository extends JpaRepository<MapEntity, Integer> {
    Optional<MapEntity> findByMapID(String s);
}
