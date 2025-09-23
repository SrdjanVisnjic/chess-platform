package com.chess.game.controller;

import com.chess.game.enums.GameStatus;
import com.chess.game.model.Game;
import com.chess.game.service.GameService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("")
@RequiredArgsConstructor
@Slf4j
public class GameController {

    private final GameService gameService;

    @PostMapping("/create")
    public ResponseEntity<Map<String,String>> createGame(@RequestBody Map<String,String> request){
        String username = request.getOrDefault("username", "anonymous");
        UUID gameId = gameService.createNewGame(username);

        Map<String, String> response = new HashMap<>();
        response.put("gameId", gameId.toString());
        response.put("message", "Game created");

        log.info("Controller returning response: {}", response);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/game/{gameId}")
    public ResponseEntity<Game> getBoard(@PathVariable UUID gameId){
        Game game = gameService.getGame(gameId);
        if(game == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(game);
    }

    @PostMapping("/move/{gameId}")
    public ResponseEntity<Map<String,Object>> makeMove(
            @PathVariable UUID gameId,
            @RequestBody Map<String,String> move) {
        log.info("Controller received move request for game {}: {}", gameId, move);
        String from = move.get("from");
        String to = move.get("to");
        String promoteTo = move.get("promoteTo");

        if (from == null || to == null) {
            log.error("Missing from/to in request: {}", move);
            return ResponseEntity.badRequest().body(Map.of("error", "Missing from or to"));
        }

        Game game = gameService.getGame(gameId);
        if (game == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Game not found"
            ));
        }

        // Don't allow moves if game is over
        if (game.getChessBoard().isGameOver()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Game is over",
                    "status", game.getChessBoard().getGameStatus()
            ));
        }

        boolean success = gameService.makeMove(gameId, from, to, promoteTo);

        if (success) {
            //Game game = gameService.getGame(gameId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "board", game.getChessBoard().getBoardUnicode(),
                    "nextTurn", game.getChessBoard().isWhiteTurn() ? "white" : "black",
                    "gameStatus", game.getChessBoard().getGameStatus(),
                    "gameOver", game.getChessBoard().isGameOver()
            ));
        } else {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Invalid move"
            ));
        }
    }

    @PostMapping("/draw/{gameId}")
    public ResponseEntity<Map<String,Object>> offerDraw(@PathVariable UUID gameId){
        Game game = gameService.getGame(gameId);
        if (game == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Game not found"
            ));
        }

        // For simplicity, auto-accept draw offers
        // In real game, you'd track who offered and wait for acceptance
        game.setGameStatus(GameStatus.DRAW);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Game ended in draw by mutual agreement",
                "board", game.getChessBoard().getBoardUnicode()
        ));
    }

    @GetMapping("/test")
    public ResponseEntity<String> test(){
        return ResponseEntity.ok("Game service is running");
    }
}
