package com.chess.game.model;

import com.chess.game.service.MoveValidator;
import lombok.Getter;

public class ChessBoard {
    @Getter
    private String [][] board = new String[8][8];
    @Getter
    private boolean whiteTurn = true;
    private MoveValidator moveValidator;
    @Getter
    private boolean whiteInCheck = false;
    @Getter
    private boolean blackInCheck = false;

    public ChessBoard(){
        initializeBoard();
        //this.moveValidator = new MoveValidator();
        printBoard();
    }

    public ChessBoard(MoveValidator moveValidator){
        initializeBoard();
        this.moveValidator = moveValidator;
        printBoard();
    }
    //Redoing this for null safety. Init all squares as empty first then add pieces
    private void initializeBoard(){
        for(int i = 0; i < 8; i++){
            for(int j = 0; j < 8 ; j++){
                board[i][j] = "--";
            }
        }
        board[0][0] = "WR"; board[0][1] = "WN"; board[0][2] = "WB"; board[0][3] = "WQ";
        board[0][4] = "WK"; board[0][5] = "WB"; board[0][6] = "WN"; board[0][7] = "WR";

        for (int i = 0; i < 8; i++) {
            board[1][i] = "WP";  // White pawns
            board[6][i] = "BP";  // Black pawns
        }

        board[7][0] = "BR"; board[7][1] = "BN"; board[7][2] = "BB"; board[7][3] = "BQ";
        board[7][4] = "BK"; board[7][5] = "BB"; board[7][6] = "BN"; board[7][7] = "BR";

    }

    public boolean makeMove(String from, String to){
        Move move = new Move(from, to);
        // Validation 1: Ensure positions are within board bounds
        if (!isValidPosition(move.getFromRow(), move.getFromColumn()) ||
                !isValidPosition(move.getToRow(), move.getToColumn())) {
            return false;
        }

        String piece = board[move.getFromRow()][move.getFromColumn()];
        // Validation 2: Ensure there's a piece to move
        if (piece.equals("--")) {
            return false;
        }
        // Validation 3: Ensure it's the correct player's turn
        if((whiteTurn && piece.charAt(0) == 'B') || (!whiteTurn && piece.charAt(0) == 'W')) {
            return false;
        }

        if(moveValidator != null) {
            //Validation 4: Check piece movement rules
            if(!moveValidator.isValidMove(board,move,whiteTurn)){
                return false;
            }
            //Validation 5: Check king safety
            if(moveValidator.wouldLeaveInCheck(board,move,whiteTurn)){
                return false;
            }
        }
        board[move.getToRow()][move.getToColumn()] = piece;
        board[move.getFromRow()][move.getFromColumn()] = "--";

        whiteTurn = !whiteTurn;
        //Update check status to see if the made move resulted in a check
        if (moveValidator != null) {
            whiteInCheck = moveValidator.isInCheck(board, true);
            blackInCheck = moveValidator.isInCheck(board, false);
        }
        return true;
    }


    //Simple validation
    private boolean isValidPosition(int row, int col){
        return row >= 0 && row < 8 && col >=0 && col < 8;
    }

    public void printBoard() {
        System.out.println("=== BOARD DEBUG ===");
        for (int i = 7; i >= 0; i--) {  // Start from top (row 7)
            System.out.print((i + 1) + " ");
            for (int j = 0; j < 8; j++) {
                String piece = board[i][j];
                if (piece == null) {
                    System.out.print("NULL ");  // Show nulls clearly
                } else {
                    System.out.print(piece + "  ");
                }
            }
            System.out.println();
        }
        System.out.println("  a  b  c  d  e  f  g  h");
    }

    public String getGameStatus() {
        if (whiteInCheck) {
            // TODO: Check if white has any legal moves
            // If not, it's checkmate
            return "White is in check!";
        }
        if (blackInCheck) {
            // TODO: Check if black has any legal moves
            // If not, it's checkmate
            return "Black is in check!";
        }
        return "Normal";
    }

}
