package com.chess.auth.dto;

import com.chess.auth.entity.User;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDTO {
    private UUID id;
    private String username;
    private String email;
    private String fullName;
    private String avatarUrl;
    private Integer eloRating;
    private Integer gamesPlayed;
    private Integer gamesWon;
    private Integer gamesDrawn;
    private Integer gamesLost;
    private Boolean isOnline;
    private LocalDateTime lastSeen;
    private Boolean emailVerified;
    private LocalDateTime createdAt;

    /**
     * Convert User entity to UserDTO
     */
    public static UserDTO fromUser(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .avatarUrl(user.getAvatarUrl())
                .eloRating(user.getEloRating())
                .gamesPlayed(user.getGamesPlayed())
                .gamesWon(user.getGamesWon())
                .gamesDrawn(user.getGamesDrawn())
                .gamesLost(user.getGamesLost())
                .isOnline(user.getIsOnline())
                .lastSeen(user.getLastSeen())
                .emailVerified(user.getEmailVerified())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
