package com.chess.game.controller;

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
    public ResponseEntity<Map<String,String>> createGame(){
        String gameId = gameService.createNewGame();
        return ResponseEntity.ok(Map.of("gameId", gameId));
    }

    @GetMapping("/board/{gameId}")
    public ResponseEntity<String[][]> getBoard(@PathVariable String gameId){
        return ResponseEntity.ok(gameService.getGame(gameId).getBoard());
    }

    @GetMapping("/test")
    public ResponseEntity<String> test(){
        return ResponseEntity.ok("Game service is running");
    }
}
