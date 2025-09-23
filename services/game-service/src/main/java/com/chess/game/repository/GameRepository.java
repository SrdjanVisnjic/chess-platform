package com.chess.game.repository;

import com.chess.game.entity.GameEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GameRepository extends JpaRepository<GameEntity,String> {
    List<GameEntity> findByWhitePlayerOrBlackPlayer(UUID whiteId, UUID blackId);
    List<GameEntity> findByStatus(String status);
}
