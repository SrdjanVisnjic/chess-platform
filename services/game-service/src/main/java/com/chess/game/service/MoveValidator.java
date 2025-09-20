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

}
