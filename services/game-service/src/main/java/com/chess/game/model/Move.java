package com.chess.game.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Move {
    private int fromRow, fromColumn, toRow, toColumn;

    //We'll be converting from Standard chess notation to array indexes
    public Move(String from, String to){
        this.fromRow = from.charAt(1) - '1';
        this.toRow = to.charAt(1) - '1';
        this.fromColumn = from.charAt(0) - 'a';
        this.toColumn = to.charAt(0) - 'a';
    }
}
