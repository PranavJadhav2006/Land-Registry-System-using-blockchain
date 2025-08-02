package com.aditya.bro.userback.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserDTO {
    private String id;
    private String username;
    private String email;
    private String walletAddress;
    private String password;
    private String registrationDate;
    private int totalLands;
    private boolean verified;
}