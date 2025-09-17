package com.chess.auth.enums;

public enum Roles {
    USER("ROLE_USER"),
    MODERATOR("ROLE_MODERATOR"),
    ADMIN("ROLE_ADMIN");

    private final String authority;
    Roles(String authority){
        this.authority = authority;
    }
    public String getAuthority(){
        return authority;
    }
}
