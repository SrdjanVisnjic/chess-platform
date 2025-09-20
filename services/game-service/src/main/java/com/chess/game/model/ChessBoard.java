package com.chess.game.model;

import com.chess.game.service.MoveValidator;

public class ChessBoard {
    private String [][] board = new String[8][8];
    private boolean whiteTurn = true;
    private MoveValidator moveValidator;

    public ChessBoard(){
        initializeBoard();
        this.moveValidator = new MoveValidator();
    }

    public ChessBoard(MoveValidator moveValidator){
        initializeBoard();
        this.moveValidator = moveValidator;
    }

    private void initializeBoard(){
        board[0][0] = "WR"; board[0][1] ="WN";board[0][2] = "WB"; board[0][3] = "WQ";
        board[0][4] = "WK"; board[0][5] = "WB"; board[0][6] = "WN"; board[0][7] = "WR";
        for(int i = 0; i< 8; i++) board[1][i] = "WP";

        for(int i = 2; i< 6; i ++){
            for(int j =0; j<8; j++){
                board[i][j] = "--";
            }
        }

        for(int i = 0 ; i < 8 ; i++)board[6][i] = "BP";
        board[7][0] = "BR"; board[7][1] = "BN"; board[7][2] = "BB"; board[7][3] = "BQ";
        board[7][4] = "BK"; board[7][5] = "BB"; board[7][6] = "BN"; board[7][7] = "BR";

    }

    public boolean makeMove(String from, String to){
        Move move = new Move(from, to);

        if (!isValidPosition(move.getFromRow(), move.getFromColumn()) ||
                !isValidPosition(move.getToRow(), move.getToColumn())) {
            return false;
        }

        String piece = board[move.getFromRow()][move.getFromColumn()];
        if (piece.equals("--")) return false;
        if((whiteTurn && piece.charAt(0) == 'B') || (!whiteTurn && piece.charAt(0) == 'W')) return false;
        if(moveValidator != null && !moveValidator.isValidMove(board,move,whiteTurn)) return false;

        board[move.getToRow()][move.getToColumn()] = piece;
        board[move.getFromRow()][move.getFromColumn()] = "--";

        whiteTurn = !whiteTurn;
        return true;
    }


    //Simple validation
    private boolean isValidPosition(int row, int col){
        return row >= 0 && row < 8 && col >=0 && col < 8;
    }

    public boolean isWhiteTurn(){
        return whiteTurn;
    }
    public String[][] getBoard() {
        return board;
    }

    public void printBoard() {
        for(int row = 7; row >= 0; row--) {
            System.out.print((row + 1) + " ");
            for(int col = 0; col < 8; col++) {
                System.out.print(board[row][col] + " ");
            }
            System.out.println();
        }
        System.out.println("  a  b  c  d  e  f  g  h");
    }

}
