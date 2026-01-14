package com.yeditepe.firstspingproject.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private Long id;
    private String token;
    private String refreshToken;
    private String tokenType = "Bearer";
    private long expiresIn;
    private String username;
    private String email;
    private Set<String> roles;
    private String message;

    public LoginResponse(String token, String username, String email, Set<String> roles) {
        this.token = token;
        this.username = username;
        this.email = email;
        this.roles = roles;
        this.message = "Login successful";
        this.tokenType = "Bearer";
    }

    public LoginResponse(Long id, String token, String refreshToken, long expiresIn, String username, String email, Set<String> roles) {
        this.id = id;
        this.token = token;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
        this.username = username;
        this.email = email;
        this.roles = roles;
        this.message = "Login successful";
        this.tokenType = "Bearer";
    }
}
