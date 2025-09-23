package com.chess.game.model;

import com.chess.game.service.MoveValidator;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    //En passant tracking
    private int enPassantColum = 1;

    //Need this for castling
    @Getter
    private boolean whiteKingMoved = false;
    @Getter
    private boolean blackKingMoved = false;
    @Getter
    private boolean whiteKRookMoved = false;
    @Getter
    private boolean whiteQRookMoved = false;
    @Getter
    private boolean blackKRookMoved = false;
    @Getter
    private boolean blackQRookMoved = false;

    //For draws
    @Getter
    private int halfMoveCount = 0; //If no pawn capture in 50 moves it's a draw
    @Getter
    private List<String> positionHistory = new ArrayList<>(); //For threefold repetition
    private Map<String, Integer> positionCounts = new HashMap<>();// Keeps count of position occurrences
    //There is also insufficient material to check mate -> draw
    //Stalemate is a draw
    //Players can draw by agreement but that will be handled in the service/controller

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

    public boolean makeMove(String from, String to, String promoteTo){
        Move move = new Move(from, to);
        // Validation 1: Ensure positions are within board bounds
        if (!isValidPosition(move.getFromRow(), move.getFromColumn()) ||
                !isValidPosition(move.getToRow(), move.getToColumn())) {
            return false;
        }

        String piece = board[move.getFromRow()][move.getFromColumn()];
        String capturedPiece = board[move.getToRow()][move.getToColumn()];

        int previousEnPassantColumn = enPassantColum;
        enPassantColum = -1;

        //This will check if the made move creates an en passant possibility
        //If you move up/down 2 squares with a pawn and have an enemy pawn to either side, it can captured by en passant
        if (piece.charAt(1) == 'P') {
            int rowDiff = move.getToRow() - move.getFromRow();
            if(Math.abs(rowDiff) == 2){
                if(piece.charAt(0) == 'W' && move.getFromRow() ==1 & move.getToRow() ==3){
                    if(hasEnemyPawnAdjacent(3,move.getToColumn(), false)){
                        enPassantColum = move.getToColumn();
                    }
                }
                else if(piece.charAt(0) == 'B' && move.getFromRow() == 6 && move.getToRow() == 4){
                    if(hasEnemyPawnAdjacent(4, move.getToColumn(), true)){
                        enPassantColum = move.getToColumn();
                    }
                }
            }
        }
        //Verify if the move is an enpassant capture
        boolean isEnPassantCapture = false;
        if(piece.charAt(1) == 'P' && previousEnPassantColumn >=0 ){
            //Diagonal move
            //Empty square
            //In the enPassant column
            if(Math.abs(move.getToColumn() - move.getFromColumn()) == 1 && move.getToColumn() == previousEnPassantColumn && capturedPiece.equals("--")){
                boolean validRank = (piece.charAt(0) == 'W' && move.getFromRow() == 4) ||
                        (piece.charAt(0) == 'B' && move.getFromRow() == 3);
                if(validRank){
                    isEnPassantCapture = true;
                    capturedPiece = "P"; // To mitigate the 50 move rule
                }
            }
        }

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
        // Track position before move for repetition
        String positionBefore = getBoardPosition();

        // Update 50-move rule counter
        if (piece.charAt(1) == 'P' || !capturedPiece.equals("--")) {
            halfMoveCount = 0;  // Reset on pawn move or capture
        } else {
            halfMoveCount++;
        }
        board[move.getToRow()][move.getToColumn()] = piece;
        board[move.getFromRow()][move.getFromColumn()] = "--";

        //Like with castling, pawn moved, but we need to update another square
        if(isEnPassantCapture){
            int capturePawnRow = piece.charAt(0) == 'W' ? 4 : 3;
            board[capturePawnRow][move.getToColumn()] = "--";
        }

        String positionAfter = getBoardPosition();
        positionHistory.add(positionAfter);
        positionCounts.put(positionAfter, positionCounts.getOrDefault(positionAfter, 0) + 1);

        //After the move is made check if it was made by a pawn and is it in the final row
        //If yes -> can promote
        if(piece.charAt(1) == 'P'){
            if((piece.charAt(0) == 'W' && move.getToRow() == 7) || (piece.charAt(0) == 'B' && move.getToRow() == 0)){
                //Null check and validity check -> default queen
                String newPiece = promoteTo != null && isValidPromotionPiece(promoteTo)? promoteTo : "Q";
                board[move.getToRow()][move.getToColumn()] = piece.charAt(0) + newPiece;
            }
        }
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
    //Overload it for moves without promotion
    public boolean makeMove(String from, String to) {
        return makeMove(from, to, null);
    }

    private boolean isValidPromotionPiece(String piece) {
        return piece.equals("Q") || piece.equals("R") ||
                piece.equals("B") || piece.equals("N");
    }
    //50 moves without pawn move or capture
    public boolean isFiftyMoveRule() {
        return halfMoveCount >= 100;  // 50 full moves = 100 half-moves
    }

    //Threefold repetition: Same position occurred 3 times
    public boolean isThreefoldRepetition() {
        for (Integer count : positionCounts.values()) {
            if (count >= 3) {
                return true;
            }
        }
        return false;
    }

    //Insufficient material check
    //Only kings -> insufficient
    //King + a bishop or knight vs king -> insufficient
    //Kings + opposite color bishops -> insufficient

    public boolean isInsufficientMaterial(){
        List<String> whitePieces = new ArrayList<>();
        List<String> blackPieces = new ArrayList<>();

        // Collect all pieces
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                String piece = board[row][col];
                if (!piece.equals("--")) {
                    if (piece.charAt(0) == 'W') {
                        whitePieces.add(piece);
                    } else {
                        blackPieces.add(piece);
                    }
                }
            }
        }
        //Only kings
        if(whitePieces.size() == 1 && blackPieces.size() == 1){
            return true;
        }

        // King + B/N vs solo king
        if((whitePieces.size() == 2 && blackPieces.size() == 1) || (whitePieces.size() == 1 && blackPieces.size() == 2)){
            List<String> twoPiece = whitePieces.size() ==2 ? whitePieces : blackPieces;
            for(String piece : twoPiece){
                if(piece.charAt(1) == 'B' || piece.charAt(1) == 'N'){
                    return true;
                }
            }
        }
        // Check if those two pieces are opposite bishops
        if(whitePieces.size() == 2 && blackPieces.size() == 2){
            boolean whiteBishop = whitePieces.stream().anyMatch(( p-> p.equals("WB")));
            boolean blackBishop = blackPieces.stream().anyMatch(p -> p.equals("BB"));
            if(whiteBishop && blackBishop){
                return true;
            }
        }

        return false;
    }
    // Check if there is an adjacent enemy pawn to the given position
    private boolean hasEnemyPawnAdjacent(int row, int col, boolean checkingForWhite) {
        String enemyPawn = checkingForWhite ? "WP" : "BP";

        // Check left
        if (col > 0 && board[row][col - 1].equals(enemyPawn)) {
            return true;
        }
        // Check right
        if (col < 7 && board[row][col + 1].equals(enemyPawn)) {
            return true;
        }
        return false;
    }

    public int getEnPassantColum(){
        return enPassantColum;
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
        if (isStalemate()) {
            return "DRAW by stalemate!";
        }
        if (isFiftyMoveRule()) {
            return "DRAW by 50-move rule!";
        }
        if (isThreefoldRepetition()) {
            return "DRAW by threefold repetition!";
        }
        if (isInsufficientMaterial()) {
            return "DRAW by insufficient material!";
        }
        return "Normal";
    }

    public boolean isDraw(){
        return  isStalemate() || isInsufficientMaterial()  || isFiftyMoveRule() || isThreefoldRepetition();
    }
    //Added this to make it nicer to look at
    public boolean isStalemate(){
        if(moveValidator == null) return false;
        return moveValidator.isStalemate(board, whiteTurn);
    }

    public boolean isGameOver() {
        if (moveValidator == null) return false;

        // Game ends on checkmate or stalemate
        return moveValidator.isCheckmate(board, true) ||
                moveValidator.isCheckmate(board, false) ||
                isDraw();
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
    //Helper method to get a string representing current position for repetition checking
    private String getBoardPosition() {
        StringBuilder sb = new StringBuilder();
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                sb.append(board[row][col]).append(",");
            }
        }
        sb.append(whiteTurn ? "W" : "B");  // Include whose turn it is

        // Castling rights
        sb.append(whiteKingMoved ? "0" : "1");
        sb.append(whiteKRookMoved? "0" : "1");
        sb.append(whiteQRookMoved ? "0" : "1");
        sb.append(blackKingMoved ? "0" : "1");
        sb.append(blackKRookMoved ? "0" : "1");
        sb.append(blackQRookMoved ? "0" : "1").append(",");

        // En passant possibility

        return sb.toString();
    }
}
