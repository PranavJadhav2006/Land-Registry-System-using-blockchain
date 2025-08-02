package com.aditya.bro.auth.repository;

import com.aditya.bro.userback.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByUsername(String username);
    Optional<User> findByWalletAddress(String walletAddress);
    Optional<User> findByEmail(String email);
}