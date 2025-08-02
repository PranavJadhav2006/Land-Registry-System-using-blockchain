package com.aditya.bro.auth.controller;

import com.aditya.bro.auth.dto.LoginRequest;
import com.aditya.bro.auth.dto.LoginResponse;
import com.aditya.bro.auth.dto.SignupRequest;
import com.aditya.bro.userback.model.User;
import com.aditya.bro.userback.repository.UserRepository;
import com.aditya.bro.auth.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest request) {
        if (userRepository.findByWalletAddress(request.getWalletAddress()).isPresent()) {
            return ResponseEntity.badRequest().body("Wallet already registered");
        }

        User user = new User();
        user.setWalletAddress(request.getWalletAddress());
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());

        userRepository.save(user);
        return ResponseEntity.ok("Signup successful");
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        return userRepository.findByEmail(request.getEmail())
                .filter(user -> passwordEncoder.matches(request.getPassword(), user.getPassword()))
                .map(user -> {
                    String token = jwtUtil.generateToken(user.getWalletAddress());
                    return ResponseEntity.ok(new LoginResponse(token, user.getRole(), user.getWalletAddress(), user.getUsername(), user.getEmail()));
                })
                .orElseGet(() -> ResponseEntity.status(401).body(new LoginResponse(null, null, null, null, null)));
    }
}