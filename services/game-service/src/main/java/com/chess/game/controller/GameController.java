package com.chess.game.controller;

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

        boolean success = gameService.makeMove(gameId, from, to);

        if(success){
            Game game = gameService.getGame(gameId);
            return ResponseEntity.ok(Map.of(
                    "success" , true,
                    "board", game.getChessBoard().getBoard(),
                    "nextTurn", game.getChessBoard().isWhiteTurn() ? "white" : "black",
                    "gameStatus", game.getChessBoard().getGameStatus(),
                    "whiteInCheck", game.getChessBoard().isWhiteInCheck(),
                    "blackInCheck", game.getChessBoard().isBlackInCheck()
            ));
        } else {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Invalid move"
            ));
        }
    }

    @GetMapping("/test")
    public ResponseEntity<String> test(){
        return ResponseEntity.ok("Game service is running");
    }
}
