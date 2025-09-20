package com.chess.game.service;

import com.chess.game.model.ChessBoard;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class GameService {
    private Map<String, ChessBoard> games = new HashMap<>();

    public String createNewGame(){
        String gameId = UUID.randomUUID().toString();
        games.put(gameId, new ChessBoard());
        return gameId;
    }

    public ChessBoard getGame(String gameId){
        return  games.get(gameId);
    }
}
