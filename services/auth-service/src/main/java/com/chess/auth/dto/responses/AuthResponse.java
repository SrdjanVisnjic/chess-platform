package com.chess.auth.dto.responses;

import com.chess.auth.dto.UserDTO;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {
    private String token;
    private String type = "Bearer";
    private UserDTO user;
}
