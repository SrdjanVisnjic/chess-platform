package com.chess.game.model;

public class ChessBoard {
    private String [][] board = new String[8][8];

    public ChessBoard(){
        initializeBoard();
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
