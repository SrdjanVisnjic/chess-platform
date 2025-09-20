package com.chess.game.service;

import com.chess.game.model.ChessBoard;
import com.chess.game.model.Game;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class GameService {
    private Map<String, Game> games = new HashMap<>();

    public String createNewGame(String playerUsername){
        String gameId = UUID.randomUUID().toString();
        Game game = new Game(gameId);
        game.setWhitePlayer(playerUsername);
        games.put(gameId, game);
        return gameId;
    }

    public boolean makeMove(String gameId, String from, String to){
        Game game = games.get(gameId);
        if(game == null) return false;
        boolean moveSuccess = game.getChessBoard().makeMove(from, to);
        if(moveSuccess) game.getMoveHistory().add(from + "-" + to);
        return moveSuccess;
    }

    public Game getGame(String gameId){
        return  games.get(gameId);
    }
}
