package com.chess.game.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "games")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameEntity {

    @Id
    //@GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "white_player_id")
    private UUID whitePlayerId;

    @Column(name = "black_player_id")
    private UUID blackPlayerId;

    @Column(name = "white_player_name")
    private String whitePlayerName;

    @Column(name = "black_player_name")
    private String blackPlayerName;

    @Column(name = "board_state", columnDefinition = "TEXT")
    private String boardState;  // JSON string of the board

    @Column(name = "move_history", columnDefinition = "TEXT")
    private String moveHistory;  // JSON array of moves

    @Column(name = "white_turn")
    private boolean whiteTurn = true;

    @Column(name = "game_status")
    private String status = "IN_PROGRESS";

    @Column(name = "winner")
    private String winner;

    @Column(name = "white_in_check")
    private boolean whiteInCheck;

    @Column(name = "black_in_check")
    private boolean blackInCheck;

    // Castling rights
    @Column(name = "white_king_moved")
    private boolean whiteKingMoved;

    @Column(name = "black_king_moved")
    private boolean blackKingMoved;

    @Column(name = "white_rook_kingside_moved")
    private boolean whiteRookKingsideMoved;

    @Column(name = "white_rook_queenside_moved")
    private boolean whiteRookQueensideMoved;

    @Column(name = "black_rook_kingside_moved")
    private boolean blackRookKingsideMoved;

    @Column(name = "black_rook_queenside_moved")
    private boolean blackRookQueensideMoved;

    @Column(name = "en_passant_column")
    private int enPassantColumn = -1;

    @Column(name = "half_move_clock")
    private int halfMoveClock = 0;

    @Column(name = "position_history", columnDefinition = "TEXT")
    private String positionHistory;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;
}
