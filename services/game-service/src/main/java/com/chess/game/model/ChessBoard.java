package com.chess.game.model;

import com.chess.game.service.MoveValidator;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

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

    //Need this for castling
    private boolean whiteKingMoved = false;
    private boolean blackKingMoved = false;
    private boolean whiteKRookMoved = false;
    private boolean whiteQRookMoved = false;
    private boolean blackKRookMoved = false;
    private boolean blackQRookMoved = false;

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
        //Check to see if the move in question is castling
        boolean isCastling = false;
        if(piece.charAt(1) == 'K' && Math.abs(move.getToColumn()-move.getFromColumn()) == 2){
            //Tracking state of movement for the validator
            isCastling = true;
            boolean kingMoved = whiteTurn? whiteKingMoved : blackKingMoved;
            boolean kingSideRookMoved = whiteTurn ? whiteKRookMoved : blackKRookMoved;
            boolean queenSideRookMoved = whiteTurn ? whiteQRookMoved: blackQRookMoved;

            if(!moveValidator.isValidCastling(board, move, whiteTurn, kingMoved, kingSideRookMoved, queenSideRookMoved)){
                return false;
            }
        }
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
        //Moving the rook and clearing the files
        //Since castling is initiated by moving the king, his position will update with his move
        if(isCastling){
            int row = move.getFromRow();
            boolean isKingSide = move.getToColumn() > move.getFromColumn();
            if(isKingSide){
                board[row][5] = board[row][7];
                board[row][7] = "--";
            } else {
                board[row][3] = board[row][0];
                board[row][0] = "--";
            }
        }

        updateCastlingRights(move, piece);

        whiteTurn = !whiteTurn;
        //Update check status to see if the made move resulted in a check
        if (moveValidator != null) {
            whiteInCheck = moveValidator.isInCheck(board, true);
            blackInCheck = moveValidator.isInCheck(board, false);
        }
        return true;
    }
    private void updateCastlingRights(Move move, String piece){
        //King moved
        if(piece.charAt(1) == 'K'){
            if(piece.charAt(0) == 'W') whiteKingMoved = true;
            else blackKingMoved = true;
        }
        //Rook moved
        if(piece.charAt(1) == 'R'){
            if(piece.charAt(0) == 'W'){
                if(move.getFromRow() == 0 && move.getFromColumn() == 0){
                    whiteQRookMoved = true;
                }
                if(move.getFromRow() == 0 && move.getFromColumn() == 7){
                    whiteKRookMoved = true;
                }
            } else {
                if(move.getFromRow() == 7 && move.getFromColumn() == 0){
                    blackQRookMoved = true;
                }
                if(move.getFromRow() == 7 && move.getFromColumn() == 7){
                    blackKRookMoved = true;
                }
            }
        }
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
            if (moveValidator != null && moveValidator.isCheckmate(board, true)) {
                return "CHECKMATE! Black wins!";
            }            // If not, it's checkmate
            return "White is in check!";
        }
        if (blackInCheck) {
            if (moveValidator != null && moveValidator.isCheckmate(board, false)) {
                return "CHECKMATE! White wins!";
            }            // If not, it's checkmate
            return "Black is in check!";
        }
        if (moveValidator != null) {
            if (moveValidator.isStalemate(board, whiteTurn)) {
                return "STALEMATE! Game is a draw.";
            }
        }
        return "Normal";
    }

    public boolean isGameOver() {
        if (moveValidator == null) return false;

        // Game ends on checkmate or stalemate
        return moveValidator.isCheckmate(board, true) ||
                moveValidator.isCheckmate(board, false) ||
                moveValidator.isStalemate(board, whiteTurn);
    }
    //Found a nice Unicode print board method
    public String getBoardUnicode() {
        StringBuilder sb = new StringBuilder();

        // Map pieces to Unicode symbols
        Map<String, String> pieceSymbols = new HashMap<>();
        pieceSymbols.put("WK", "♔");
        pieceSymbols.put("WQ", "♕");
        pieceSymbols.put("WR", "♖");
        pieceSymbols.put("WB", "♗");
        pieceSymbols.put("WN", "♘");
        pieceSymbols.put("WP", "♙");
        pieceSymbols.put("BK", "♚");
        pieceSymbols.put("BQ", "♛");
        pieceSymbols.put("BR", "♜");
        pieceSymbols.put("BB", "♝");
        pieceSymbols.put("BN", "♞");
        pieceSymbols.put("BP", "♟");
        pieceSymbols.put("--", "·");

        sb.append("\n   a b c d e f g h\n");
        sb.append("  ┌─────────────────┐\n");

        for (int row = 7; row >= 0; row--) {
            sb.append(row + 1).append(" │ ");

            for (int col = 0; col < 8; col++) {
                String piece = board[row][col];
                String symbol = pieceSymbols.getOrDefault(piece, "?");
                sb.append(symbol).append(" ");
            }

            sb.append("│ ").append(row + 1).append("\n");
        }

        sb.append("  └─────────────────┘\n");
        sb.append("   a b c d e f g h\n");

        return sb.toString();
    }
}
