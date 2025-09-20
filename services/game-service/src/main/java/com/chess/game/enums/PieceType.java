package com.chess.game.enums;

public enum PieceType {
    PAWN('P'),
    KNIGHT('N'),
    BISHOP('B'),
    ROOK('R'),
    QUEEN('Q'),
    KING('K');

    private final char symbol;
    PieceType(char symbol){
        this.symbol = symbol;
    }

    public char getSymbol() {
        return symbol;
    }

    public static PieceType fromSymbol(char symbol){
        for(PieceType p : values()){
            if(p.symbol == symbol){
                return p;
            }
        }
        return null;
    }
}
