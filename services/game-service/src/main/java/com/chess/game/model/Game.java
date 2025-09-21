package com.chess.game.model;

import com.chess.game.enums.GameStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class Game {
    private String id;
    private String whitePlayer;
    private String blackPlayer;
    private ChessBoard chessBoard;
    private List<String> moveHistory;
    private GameStatus gameStatus;
    private LocalDateTime createdAt;

    public Game(String id){
        this.id = id;
        this.moveHistory = new ArrayList<>();
        this.gameStatus = GameStatus.WAITING_FOR_PLAYER;
        this.createdAt = LocalDateTime.now();
    }
}
