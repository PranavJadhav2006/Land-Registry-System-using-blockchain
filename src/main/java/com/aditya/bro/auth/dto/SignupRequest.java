package com.aditya.bro.auth.dto;

import lombok.Data;

@Data
public class SignupRequest {
    private String walletAddress;
    private String username;
    private String email;
    private String password;
    private String role;

    public String getWalletAddress() {
        return walletAddress;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getRole() {
        return role;
    }

    public String getEmail() {
        return email;
    }
}
