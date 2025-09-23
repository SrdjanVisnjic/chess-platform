CREATE TABLE games (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    white_player_id UUID,
    black_player_id UUID,
    white_player_name VARCHAR(100),
    black_player_name VARCHAR(100),
    board_state TEXT NOT NULL,  -- JSON representation of board
    move_history TEXT,           -- JSON array of moves
    white_turn BOOLEAN DEFAULT true,
    game_status VARCHAR(50) DEFAULT 'IN_PROGRESS',
    winner VARCHAR(10),  -- WHITE, BLACK, or DRAW

    -- Game state tracking
    white_in_check BOOLEAN DEFAULT false,
    black_in_check BOOLEAN DEFAULT false,

    -- Castling rights
    white_king_moved BOOLEAN DEFAULT false,
    black_king_moved BOOLEAN DEFAULT false,
    white_rook_kingside_moved BOOLEAN DEFAULT false,
    white_rook_queenside_moved BOOLEAN DEFAULT false,
    black_rook_kingside_moved BOOLEAN DEFAULT false,
    black_rook_queenside_moved BOOLEAN DEFAULT false,

    -- En passant
    en_passant_column INTEGER DEFAULT -1,

    -- Draw tracking
    half_move_clock INTEGER DEFAULT 0,
    position_history TEXT,  -- For threefold repetition

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP
);

CREATE INDEX idx_games_white_player ON games(white_player_id);
CREATE INDEX idx_games_black_player ON games(black_player_id);
CREATE INDEX idx_games_status ON games(game_status);