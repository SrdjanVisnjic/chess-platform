package com.chess.game.service;

import com.chess.game.enums.PieceType;
import org.springframework.stereotype.Component;
import com.chess.game.model.Move;

@Component
public class MoveValidator {

    public boolean isValidMove(String[][]board, Move move , boolean whiteTurn){
        String piece = board[move.getFromRow()][move.getFromColumn()];
        String target = board[move.getToRow()][move.getToColumn()];

        //Validate taking by checking piece colors
        if(!target.equals("--")){
            char pieceColor = piece.charAt(0);
            char targetColor = target.charAt(0);
            //Cant take your own piece
            if(pieceColor == targetColor){
                return false;
            }
        }

        PieceType pieceType = PieceType.fromSymbol(piece.charAt(1));
        if(pieceType == null) return false;

        switch (pieceType) {
            case PAWN -> {
                return isValidPawnMove(board, move, whiteTurn);
            }
            case KNIGHT -> {
                return isValidKnightMove(move);
            }
            case BISHOP -> {
                return isValidBishopMove(board, move);
            }
            case ROOK -> {
                return isValidRookMove(board, move);
            }
            case QUEEN -> {
                return isValidQueenMove(board, move);
            }
            case KING -> {
                return isValidKingMove(move);
            }
            default -> {
                return false;
            }
        }
    }

    private boolean isValidPawnMove(String[][] board, Move move, boolean isWhite){
        //No need to abs this since pawns can only move up and down
        int rowDiff = move.getToRow() - move.getFromRow();
        int collDiff = Math.abs(move.getToColumn() - move.getFromColumn());
        String target = board[move.getToRow()][move.getToColumn()];

        int direction = isWhite? 1:-1;

        if (collDiff == 0){
            //Check if there is a piece in front in the same column
            if(!target.equals("--")) return false;
            //Check if pawn was moved one square in front in the column
            if(rowDiff == direction) return true;
            //This is for starting moves, pawn can move 2 squares provided it is not blocked by another piece
            if((isWhite && move.getFromRow() ==1) || (!isWhite && move.getFromRow() == 6)){
                if(rowDiff == 2*direction){
                    boolean frontSquareNotBlocked = board[move.getFromRow() + direction][move.getFromColumn()].equals("--");
                    return frontSquareNotBlocked;
                }
            }
        }
        //This is for taking pieces. Can move diagonally by 1 if it is not an empty square
        if(collDiff == 1 && rowDiff ==direction){
            return !target.equals("--");
        }
        return false;
    }
    private boolean isValidKnightMove(Move move){
        int rowDiff = Math.abs(move.getToRow() - move.getFromRow());
        int colDiff = Math.abs(move.getToColumn() - move.getFromColumn());
        //Simplest way of doing the 2 squares in one direction and 1 square in another
        return(rowDiff == 2 && colDiff == 1) || (rowDiff == 1 && colDiff ==2);
    }
    private boolean isValidBishopMove(String[][] board, Move move){
        int rowDiff = Math.abs(move.getToRow() - move.getFromRow());
        int colDiff = Math.abs(move.getToColumn() - move.getFromColumn());

        //Diagonal
        if(rowDiff != colDiff) return false;

        return isPathClear(board, move);
    }
    private boolean isValidRookMove(String[][] board, Move move){
        //Straight line check
        if(move.getFromRow() != move.getToRow() && move.getFromColumn() != move.getToColumn()) return false;
        return isPathClear(board, move);
    }
    private boolean isValidQueenMove(String[][] board, Move move){
        //Can move like both so just use their methods
        return isValidBishopMove(board, move) || isValidRookMove(board, move);
    }
    private boolean isValidKingMove(Move move){
        int rowDiff = Math.abs(move.getToRow() - move.getFromRow());
        int colDiff = Math.abs(move.getToColumn() - move.getFromColumn());

        // King moves one square in any direction
        return rowDiff <= 1 && colDiff <= 1;
    }


    private boolean isPathClear(String[][] board, Move move){
        //We check like this so we don't have to specify white/black orientation
        int rowStep = Integer.compare(move.getToRow(), move.getFromRow());
        int colStep = Integer.compare(move.getToColumn(), move.getFromColumn());

        //Go to the next square
        int currentRow = move.getFromRow() + rowStep;
        int currentColumn = move.getFromColumn() + colStep;

        //Increment step by step, check if the square is empty, if not return false
        while(currentRow != move.getToRow() || currentColumn != move.getToColumn()){
            if(!board[currentRow][currentColumn].equals("--")) return false;
            currentRow += rowStep;
            currentColumn += colStep;
        }
        return true;
    }
    //After every move we need to check if the king is safe. For that we need to know where the king is
    //Inefficient O(n2) search #TODO optimize later
    public int[] findKing(String[][]board , boolean isWhite){
        char kingColor = isWhite ? 'W' : 'B';
        for(int i = 0; i < 8 ; i++){
            for(int j = 0; j < 8 ; j++){
                String piece = board[i][j];
                if(piece == null) {
                    System.out.println("Null found" + i + j);
                    continue;
                }
                if(!piece.equals("--") &&
                        piece.charAt(0) == kingColor &&
                        piece.charAt(1) == 'K'){
                    return new int[]{i,j};
                }
            }
        }
        return null;
    }

    //We check if the square is under attack if any piece can move to that square
    private boolean isSquareUnderAttack(String [][] board, int row, int col, boolean isWhite){
        if(board == null){
            System.out.println("Board is NULL");
            return false;
        }

        char attacker = isWhite ? 'W' : 'B';
       //Check every square for potential attackers
        for (int i = 0; i < 8 ; i++){
            for(int j = 0; j < 8; j++){
                String piece = board[i][j];
                //Skip empty and friendly squares
                if(piece.equals("--") || piece.charAt(0) != attacker){
                    continue;
                }
                //Create a test move for the piece to see if it can move to our target square
                Move testMove = new Move();
                testMove.setFromRow(i);
                testMove.setFromColumn(j);
                testMove.setToRow(row);
                testMove.setToColumn(col);
                //Check if the piece can legally attack
                if(canPieceAttack(board,testMove,piece)) return true;
            }
        }
        return false;
    }
    //Most pieces attack how they move, pawns are the exception
    private boolean canPieceAttack(String[][] board, Move move, String piece){
        PieceType pieceType = PieceType.fromSymbol(piece.charAt(1));

        switch (pieceType){
            case PAWN -> {
                return  canPawnAttack(move, piece.charAt(0) == 'W');
            }
            case KNIGHT -> {
                return isValidKnightMove(move);
            }
            case BISHOP -> {
                return isValidBishopMove(board, move);
            }
            case ROOK -> {
                return isValidRookMove(board, move);
            }
            case QUEEN ->
            {
                return isValidQueenMove(board, move);
            }
            case KING -> {
                return isValidKingMove(move);
            }
            default -> {
                return false;
            }
        }
    }
    //If a pawn is changing columns and going up/down the board, it is attacking a piece
    private boolean canPawnAttack(Move move, boolean isWhite){
        int rowDiff = move.getToRow() - move.getFromRow();
        int colDiff = Math.abs(move.getToColumn() - move.getFromColumn());

        int expectedDirection = isWhite? 1 : -1;
        return rowDiff == expectedDirection && colDiff == 1;
    }

    public boolean isInCheck(String[][] board, boolean isWhite){
        int[] kingPos = findKing(board,isWhite);
        //Edge case if a king doesn't exist, will never get to this
        if(kingPos == null) return false;
        //If kings square is attacked by the opposite color, it's check
        return isSquareUnderAttack(board,kingPos[0],kingPos[1], !isWhite);
    }

    //See if the move we make will leave us in check
    public boolean wouldLeaveInCheck(String[][]board, Move move, boolean isWhite){
        String[][] temp = copyBoard(board);
        //Make the move and check if the new board state results in check
        String piece = temp[move.getFromRow()][move.getFromColumn()];
        temp[move.getToRow()][move.getToColumn()] = piece;
        temp[move.getFromRow()][move.getFromColumn()] = "--";
        return  isInCheck(temp, isWhite);
    }
    //Brute force checkmate detection. We check if king is in check, try every possible move for that player, if none get the king out of check it's checkmate
    //O(n4) yikes, at least it's limited to 4096. #TODO Check how engines optimize this
    public boolean hasLegalMove(String[][] board, boolean isWhite){
        //We try every piece of the current player
        for(int i = 0; i < 8; i++){
            for(int j = 0 ; j < 8 ; j++){
                String piece = board[i][j];
                //Skip if empty or wrong color
                if(piece == null || piece.equals("--")) continue;
                if((isWhite && piece.charAt(0) != 'W')||(!isWhite && piece.charAt(0) != 'B')) continue;
                //Move it to every square
                for(int k = 0; k < 8; k++){
                    for(int l = 0; l < 8 ;l++){
                        Move testMove = new Move();
                        testMove.setFromRow(i);
                        testMove.setFromColumn(j);
                        testMove.setToRow(k);
                        testMove.setToColumn(l);
                        //Skip invalid moves
                        if(!isValidMove(board,testMove,isWhite)) continue;
                        //If the move gets us out of check, we've found at least one legal move
                        if(!wouldLeaveInCheck(board,testMove,isWhite)){
                            System.out.println("Legal move found: " +
                                    i + "," + j + " to " + k + "," + l);
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
    //Check + no legal moves = checkmate
    public boolean isCheckmate(String[][]board, boolean isWhite){
        return isInCheck(board, isWhite) && !hasLegalMove(board, isWhite);
    }
    //Not check + no legal moves = stalemate
    public boolean isStalemate(String[][]board, boolean isWhite){
        return !isInCheck(board, isWhite) && !hasLegalMove(board, isWhite);
    }

    //Helper method to deep copy the matrix
    private String [][] copyBoard(String[][] board){
        String[][] copy = new String[8][8];
        for(int i = 0; i < 8; i++){
           for(int j = 0; j < 8 ; j++){
               copy[i][j] = board[i][j];
           }
        }
        return copy;
    }
}
