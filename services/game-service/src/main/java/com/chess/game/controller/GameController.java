package com.chess.game.controller;

import com.chess.game.enums.GameStatus;
import com.chess.game.model.Game;
import com.chess.game.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("")
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;

    @PostMapping("/create")
    public ResponseEntity<Map<String,String>> createGame(@RequestBody Map<String,String> request){
        String username = request.getOrDefault("username", "anonymous");
        String gameId = gameService.createNewGame(username);
        return ResponseEntity.ok(Map.of("gameId", gameId, "message", "Game created"));
    }

    @GetMapping("/game/{gameId}")
    public ResponseEntity<Game> getBoard(@PathVariable String gameId){
        Game game = gameService.getGame(gameId);
        if(game == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(game);
    }

    @PostMapping("/move/{gameId}")
    public ResponseEntity<Map<String,Object>> makeMove(
            @PathVariable String gameId,
            @RequestBody Map<String,String> move){
        String from = move.get("from");
        String to = move.get("to");
        String promoteTo = move.get("promoteTo");

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
    public ResponseEntity<Map<String,Object>> offerDraw(@PathVariable String gameId){
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
