package com.chess.game.repository;

import com.chess.game.entity.GameEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GameRepository extends JpaRepository<GameEntity,String> {
    List<GameEntity> findByStatus(String status);
    List<GameEntity> findByWhitePlayerIdOrBlackPlayerId(UUID playerId, UUID playerId1);
    Optional<GameEntity> findById(UUID id);
}
