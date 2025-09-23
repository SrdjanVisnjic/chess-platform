package com.chess.game.model;

import com.chess.game.enums.GameStatus;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
public class Game {
    private UUID id;
    private String whitePlayer;
    private String blackPlayer;
    private ChessBoard chessBoard;
    private List<String> moveHistory;
    private GameStatus gameStatus;
    private LocalDateTime createdAt;

    public Game(UUID id){
        this.id = id;
        this.moveHistory = new ArrayList<>();
        this.gameStatus = GameStatus.WAITING_FOR_PLAYER;
        this.createdAt = LocalDateTime.now();
    }
}
