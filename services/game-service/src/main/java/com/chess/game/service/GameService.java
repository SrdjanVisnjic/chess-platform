package com.chess.game.service;

import com.chess.game.entity.GameEntity;
import com.chess.game.enums.GameStatus;
import com.chess.game.model.ChessBoard;
import com.chess.game.model.Game;
import com.chess.game.repository.GameRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.transaction.annotation.Transactional;  // Use Spring's, not Jakarta's
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class GameService {
    //private Map<UUID, Game> games = new HashMap<>();
    private final MoveValidator moveValidator;
    private final GameRepository gameRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    @PersistenceContext
    private EntityManager entityManager;

    public UUID createNewGame(String playerUsername){
        UUID gameId = UUID.randomUUID();
        log.info("Creating game with ID: {}", gameId);

        //In memory
        Game game = new Game(gameId);
        log.info("Game object has ID: {}", game.getId());
        game.setChessBoard(new ChessBoard(moveValidator));
        game.setWhitePlayer(playerUsername);
        GameEntity entity = new GameEntity();
        entity.setId(gameId);
        entity.setWhitePlayerName(playerUsername);
        entity.setBoardState(serializeBoard(game.getChessBoard()));
        entity.setMoveHistory("[]");
        entity.setStatus("WAITING_FOR_PLAYER");
        entity.setWhiteTurn(true);

        // Initialize chess state
        entity.setEnPassantColumn(-1);
        entity.setHalfMoveClock(0);
        entity.setWhiteKingMoved(false);
        entity.setBlackKingMoved(false);
        entity.setWhiteRookKingsideMoved(false);
        entity.setWhiteRookQueensideMoved(false);
        entity.setBlackRookKingsideMoved(false);
        entity.setBlackRookQueensideMoved(false);

        //Save to db
        gameRepository.save(entity);
        //games.put(gameId, game);
        log.info("Saved entity with ID: {}", entity.getId());
        return gameId;
    }
    @Transactional
    public boolean makeMove(UUID gameId, String from, String to, String promoteTo) {
        log.info("Making move in game {}: {} -> {}", gameId, from, to);

        try {
            // Load entity directly from database
            GameEntity entity = gameRepository.findById(gameId)
                    .orElseThrow(() -> new IllegalArgumentException("Game not found: " + gameId));

            log.debug("Loaded entity. Board state length: {}, Move history: {}",
                    entity.getBoardState().length(), entity.getMoveHistory());

            // Recreate board from entity
            ChessBoard board = new ChessBoard(moveValidator);

            // Deserialize board state
            try {
                String[][] boardArray = objectMapper.readValue(entity.getBoardState(), String[][].class);
                board.setBoard(boardArray);
            } catch (JsonProcessingException e) {
                log.error("Failed to deserialize board state: {}", entity.getBoardState(), e);
                return false;
            }

            // Restore board state
            board.setWhiteTurn(entity.isWhiteTurn());
            board.setWhiteInCheck(entity.isWhiteInCheck());
            board.setBlackInCheck(entity.isBlackInCheck());
            board.setWhiteKingMoved(entity.isWhiteKingMoved());
            board.setBlackKingMoved(entity.isBlackKingMoved());
            board.setWhiteKRookMoved(entity.isWhiteRookKingsideMoved());
            board.setWhiteQRookMoved(entity.isWhiteRookQueensideMoved());
            board.setBlackKRookMoved(entity.isBlackRookKingsideMoved());
            board.setBlackQRookMoved(entity.isBlackRookQueensideMoved());
            board.setEnPassantColum(entity.getEnPassantColumn());
            board.setHalfMoveCount(entity.getHalfMoveClock());

            // Make the move
            log.debug("Attempting move on board");
            boolean success = board.makeMove(from, to, promoteTo);

            if (success) {
                log.debug("Move successful, updating entity");

                // Update move history
                List<String> moveHistory;
                try {
                    if (entity.getMoveHistory() == null || entity.getMoveHistory().equals("[]")) {
                        moveHistory = new ArrayList<>();
                    } else {
                        moveHistory = objectMapper.readValue(entity.getMoveHistory(),
                                new TypeReference<List<String>>() {});
                    }
                } catch (JsonProcessingException e) {
                    log.error("Failed to deserialize move history: {}", entity.getMoveHistory(), e);
                    moveHistory = new ArrayList<>();
                }

                String moveNotation = from + "-" + to;
                if (promoteTo != null) moveNotation += "=" + promoteTo;
                moveHistory.add(moveNotation);

                // Serialize updated state
                try {
                    entity.setBoardState(objectMapper.writeValueAsString(board.getBoard()));
                    entity.setMoveHistory(objectMapper.writeValueAsString(moveHistory));
                } catch (JsonProcessingException e) {
                    log.error("Failed to serialize board/moves", e);
                    return false;
                }

                // Update other fields
                entity.setWhiteTurn(board.isWhiteTurn());
                entity.setWhiteInCheck(board.isWhiteInCheck());
                entity.setBlackInCheck(board.isBlackInCheck());
                entity.setWhiteKingMoved(board.isWhiteKingMoved());
                entity.setBlackKingMoved(board.isBlackKingMoved());
                entity.setWhiteRookKingsideMoved(board.isWhiteKRookMoved());
                entity.setWhiteRookQueensideMoved(board.isWhiteQRookMoved());
                entity.setBlackRookKingsideMoved(board.isBlackKRookMoved());
                entity.setBlackRookQueensideMoved(board.isBlackQRookMoved());
                entity.setEnPassantColumn(board.getEnPassantColum());
                entity.setHalfMoveClock(board.getHalfMoveCount());

                // Save - this should trigger UPDATE
                log.debug("Saving entity to database");
                GameEntity savedEntity = gameRepository.save(entity);
                entityManager.flush();
                log.info("Move saved successfully. Move history: {}", savedEntity.getMoveHistory());

                return true;
            } else {
                log.warn("Move validation failed: {} -> {}", from, to);
                return false;
            }
        } catch (Exception e) {
            log.error("Error making move", e);
            return false;
        }
    }
    //Overload for previous attempt
    public boolean makeMove(UUID gameId, String from, String to) throws JsonProcessingException {
        return makeMove(gameId, from, to,null);
    }

    public Game getGame(UUID gameId){
       // if(games.containsKey(gameId))
          //  return  games.get(gameId);
        Optional<GameEntity> entity = gameRepository.findById(gameId);
        if(entity.isPresent()){
            Game game = loadGameFromEntity(entity.get());
            if ("IN_PROGRESS".equals(entity.get().getStatus()) ||
                    "WAITING_FOR_PLAYER".equals(entity.get().getStatus())) {
               // games.put(gameId, game);
            }
            return game;
        }
        return null;
    }
    @Transactional
    private void updateGameInDatabase(Game game) {
        gameRepository.findById(game.getId()).ifPresent(entity -> {
            ChessBoard board = game.getChessBoard();

            // Update board state
            entity.setBoardState(serializeBoard(board));
            entity.setMoveHistory(serializeMoveHistory(game.getMoveHistory()));

            // Update turn and check status
            entity.setWhiteTurn(board.isWhiteTurn());
            entity.setWhiteInCheck(board.isWhiteInCheck());
            entity.setBlackInCheck(board.isBlackInCheck());

            // Update castling rights
            entity.setWhiteKingMoved(board.isWhiteKingMoved());
            entity.setBlackKingMoved(board.isBlackKingMoved());
            entity.setWhiteRookKingsideMoved(board.isWhiteKRookMoved());
            entity.setWhiteRookQueensideMoved(board.isWhiteQRookMoved());
            entity.setBlackRookKingsideMoved(board.isBlackKRookMoved());
            entity.setBlackRookQueensideMoved(board.isBlackQRookMoved());

            // Update en passant
            entity.setEnPassantColumn(board.getEnPassantColum());

            // Update draw tracking
            entity.setHalfMoveClock(board.getHalfMoveCount());
            entity.setPositionHistory(serializePositionHistory(board.getPositionHistory()));

            // Update game status
            if (board.isGameOver()) {
                entity.setStatus("COMPLETED");
                entity.setCompletedAt(LocalDateTime.now());

                // Determine winner
                String status = board.getGameStatus();
                if (status.contains("White wins")) {
                    entity.setWinner("WHITE");
                } else if (status.contains("Black wins")) {
                    entity.setWinner("BLACK");
                } else {
                    entity.setWinner("DRAW");
                }

                // Remove from cache when game ends
               // games.remove(game.getId());
            }

            gameRepository.save(entity);
            entityManager.flush();
            log.debug("Updated game {} in database", entity.getId());
        });
    }

    private Game loadGameFromEntity(GameEntity entity){
        Game game = new Game(entity.getId());
        game.setWhitePlayer(entity.getWhitePlayerName());
        game.setBlackPlayer(entity.getBlackPlayerName());

        // Create board with validator
        ChessBoard board = new ChessBoard(moveValidator);

        // Deserialize and set board state
        try {
            String[][] boardArray = objectMapper.readValue(
                    entity.getBoardState(),
                    String[][].class
            );
            board.setBoard(boardArray);
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize board state for game {}", entity.getId(), e);
        }

        // Restore turn and check status
        board.setWhiteTurn(entity.isWhiteTurn());
        board.setWhiteInCheck(entity.isWhiteInCheck());
        board.setBlackInCheck(entity.isBlackInCheck());

        // Restore castling rights
        board.setWhiteKingMoved(entity.isWhiteKingMoved());
        board.setBlackKingMoved(entity.isBlackKingMoved());
        board.setWhiteKRookMoved(entity.isWhiteRookKingsideMoved());
        board.setWhiteQRookMoved(entity.isWhiteRookQueensideMoved());
        board.setBlackKRookMoved(entity.isBlackRookKingsideMoved());
        board.setBlackQRookMoved(entity.isBlackRookQueensideMoved());

        // Restore en passant
        board.setEnPassantColum(entity.getEnPassantColumn());

        // Restore draw tracking
        board.setHalfMoveCount(entity.getHalfMoveClock());
        if (entity.getPositionHistory() != null) {
            try {
                List<String> positionHistory = objectMapper.readValue(
                        entity.getPositionHistory(),
                        new TypeReference<List<String>>() {}
                );
                board.setPositionHistory(positionHistory);
            } catch (JsonProcessingException e) {
                log.error("Failed to deserialize position history for game {}", entity.getId(), e);
                board.setPositionHistory(new ArrayList<>());
            }
        } else {
            board.setPositionHistory(new ArrayList<>());
        }

        // Set the board on the game
        game.setChessBoard(board);

        // Deserialize move history
        try {
            List<String> moves = objectMapper.readValue(
                    entity.getMoveHistory(),
                    new TypeReference<List<String>>() {}
            );
            game.setMoveHistory(moves);
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize move history for game {}", entity.getId(), e);
            game.setMoveHistory(new ArrayList<>());
        }

        log.debug("Loaded game {} from database", entity.getId());
        return game;
    }

    public List<GameEntity> getPlayerGames(UUID playerId) {
        return gameRepository.findByWhitePlayerIdOrBlackPlayerId(playerId, playerId);
    }

    public List<GameEntity> getActiveGames() {
        return gameRepository.findByStatus("IN_PROGRESS");
    }

    public void joinGame(UUID gameId, String blackPlayerName) {
        Game game = getGame(gameId);
        if (game == null) {
            throw new IllegalArgumentException("Game not found: " + gameId);
        }

        if (game.getBlackPlayer() != null) {
            throw new IllegalStateException("Game already has two players");
        }

        game.setBlackPlayer(blackPlayerName);

        // Update database
        gameRepository.findById(gameId).ifPresent(entity -> {
            entity.setBlackPlayerName(blackPlayerName);
            entity.setStatus("IN_PROGRESS");
            gameRepository.save(entity);
        });

        log.info("Player {} joined game {}", blackPlayerName, gameId);
    }

    // Helper methods
    private String serializeBoard(ChessBoard board) {
        try {
            return objectMapper.writeValueAsString(board.getBoard());
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize board", e);
            return "[]";
        }
    }

    private String serializeMoveHistory(List<String> moves) {
        try {
            return objectMapper.writeValueAsString(moves);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize moves", e);
            return "[]";
        }
    }

    private String serializePositionHistory(List<String> positions) {
        if (positions == null) return "[]";
        try {
            return objectMapper.writeValueAsString(positions);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize position history", e);
            return "[]";
        }

    }
    private List<String> deserializeMoveHistory(String moveHistoryJson) {
        if (moveHistoryJson == null || moveHistoryJson.equals("[]")) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(moveHistoryJson, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize move history", e);
            return new ArrayList<>();
        }
    }
}
