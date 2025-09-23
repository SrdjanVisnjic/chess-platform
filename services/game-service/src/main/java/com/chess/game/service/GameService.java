package com.chess.game.service;

import com.chess.game.entity.GameEntity;
import com.chess.game.enums.GameStatus;
import com.chess.game.model.ChessBoard;
import com.chess.game.model.Game;
import com.chess.game.repository.GameRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class GameService {
    private Map<String, Game> games = new HashMap<>();
    private final MoveValidator moveValidator;
    private final GameRepository gameRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String createNewGame(String playerUsername){
        String gameId = UUID.randomUUID().toString();

        //In memory
        Game game = new Game(gameId);
        game.setChessBoard(new ChessBoard(moveValidator));
        game.setWhitePlayer(playerUsername);

        //Save to db
        GameEntity gameEntity = convertToEntity(game);
        games.put(gameId, game);
        return gameId;
    }

    public boolean makeMove(String gameId, String from, String to , String promoteTo){
        Game game = games.get(gameId);
        if(game == null)
            return false;
        boolean moveSuccess = game.getChessBoard().makeMove(from, to, promoteTo);
        if(moveSuccess) {
            String moveNotation = from + "-" + to;
            if(promoteTo != null){
                moveNotation +="="+promoteTo;
            }
            game.getMoveHistory().add(moveNotation);
        }
        return moveSuccess;
    }
    //Overload for previous attempt
    public boolean makeMove(String gameId, String from, String to){
        return makeMove(gameId, from, to,null);
    }

    public Game getGame(String gameId){
        if(games.containsKey(gameId))
            return  games.get(gameId);
        Optional<GameEntity> entity = gameRepository.findById(gameId);
        if(entity.isPresent()){
            Game game = loadGameFromEntity(entity.get());
            games.put(gameId, game);
            return game;
        }
        return null;
    }

    private GameEntity convertToEntity(Game game){
        GameEntity entity = new GameEntity();
        entity.setId(UUID.fromString(game.getId()));
        entity.setWhitePlayerName(game.getWhitePlayer());
        entity.setBlackPlayerName(game.getBlackPlayer());
        entity.setGameStatus(game.getGameStatus().toString());

        // Serialize board state
        try {
            entity.setBoardState(objectMapper.writeValueAsString(game.getChessBoard().getBoard()));
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize board", e);
        }
        // Serialize move history
        entity.setMoveHistory(String.join(",", game.getMoveHistory()));

        //Copy board state flags
        ChessBoard board = game.getChessBoard();
        entity.setWhiteTurn(board.isWhiteTurn());
        entity.setWhiteInCheck(board.isWhiteInCheck());
        entity.setBlackInCheck(board.isBlackInCheck());
        entity.setWhiteKingMoved(board.isWhiteKingMoved());
        entity.setBlackKingMoved(board.isBlackKingMoved());
        entity.setWhiteRookQueensideMoved(board.isWhiteQRookMoved());
        entity.setWhiteRookKingsideMoved(board.isWhiteKRookMoved());
        entity.setBlackRookKingsideMoved(board.isBlackKRookMoved());
        entity.setBlackRookQueensideMoved(board.isBlackQRookMoved());
        entity.setEnPassantColumn(board.getEnPassantColum());
        entity.setHalfMoveClock(board.getHalfMoveCount());
        entity.setPositionHistory(board.getPositionHistory().toString());

        return entity;
    }

    private Game loadGameFromEntity(GameEntity entity){
        Game game = new Game(entity.getId().toString());
        return game;
    }
}
