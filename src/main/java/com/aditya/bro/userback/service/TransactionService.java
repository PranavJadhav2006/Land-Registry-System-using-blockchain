package com.aditya.bro.userback.service;

import com.aditya.bro.userback.dto.TransactionDTO;
import com.aditya.bro.userback.exception.ResourceNotFoundException;
import com.aditya.bro.land.entity.LandParcel; // Changed import
import com.aditya.bro.land.entity.LandParcel.Transaction; // Changed import
import com.aditya.bro.userback.model.User;
import com.aditya.bro.land.repository.LandRepository; // Changed import
import com.aditya.bro.auth.repository.UserRepository; // Changed import
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionService {
    private final LandRepository landRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    public TransactionDTO initiateTransfer(TransactionDTO transactionDTO) {
        // Validate land exists
        LandParcel land = landRepository.findBySurveyNumber(transactionDTO.getLandId())
                .orElseThrow(() -> new ResourceNotFoundException("Land not found with id: " + transactionDTO.getLandId()));

        // Validate from user exists
        User fromUser = userRepository.findById(transactionDTO.getFromUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + transactionDTO.getFromUserId()));

        // Validate to user exists
        User toUser = userRepository.findByEmail(transactionDTO.getToUserEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + transactionDTO.getToUserEmail()));

        // Create transaction
        Transaction transaction = new Transaction();
        transaction.setId(generateTransactionId());
        transaction.setLandId(land.getSurveyNumber()); // Use surveyNumber as ID
        transaction.setFrom(fromUser.getId()); // Use from for fromUser
        transaction.setTo(toUser.getId()); // Use to for toUser
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setType(transactionDTO.getType());
        transaction.setAmount(transactionDTO.getAmount());
        transaction.setStatus("PENDING");
        transaction.setNotes(transactionDTO.getNotes());

        // Add transaction to land's history
        if (land.getTransactionHistory() == null) {
            land.setTransactionHistory(new java.util.ArrayList<>());
        }
        land.getTransactionHistory().add(transaction);
        landRepository.save(land);

        return convertToDto(transaction, land, fromUser, toUser);
    }

    public List<TransactionDTO> getTransactionsByUser(String userId) {
        // Get all lands where user is involved
        List<LandParcel> relevantLands = landRepository.findByCurrentOwnerId(userId); // Changed method

        return relevantLands.stream()
                .flatMap(land -> land.getTransactionHistory().stream()
                        .filter(t -> userId.equals(t.getFrom()) || userId.equals(t.getTo()))) // Use getFrom/getTo
                .map(t -> {
                    User fromUser = t.getFrom() != null ?
                            userRepository.findById(t.getFrom()).orElse(null) : null; // Use getFrom
                    User toUser = t.getTo() != null ?
                            userRepository.findById(t.getTo()).orElse(null) : null; // Use getTo
                    return convertToDto(t, landRepository.findBySurveyNumber(t.getLandId()).orElse(null), fromUser, toUser); // Use findBySurveyNumber
                })
                .collect(Collectors.toList());
    }

    public List<TransactionDTO> getTransactionsByLand(String landId) {
        LandParcel land = landRepository.findBySurveyNumber(landId) // Use findBySurveyNumber
                .orElseThrow(() -> new ResourceNotFoundException("Land not found with id: " + landId));

        return land.getTransactionHistory().stream()
                .map(t -> {
                    User fromUser = t.getFrom() != null ?
                            userRepository.findById(t.getFrom()).orElse(null) : null; // Use getFrom
                    User toUser = t.getTo() != null ?
                            userRepository.findById(t.getTo()).orElse(null) : null; // Use getTo
                    return convertToDto(t, land, fromUser, toUser);
                })
                .collect(Collectors.toList());
    }

    public TransactionDTO updateTransactionStatus(String landId, String transactionId, String status) {
        LandParcel land = landRepository.findBySurveyNumber(landId) // Use findBySurveyNumber
                .orElseThrow(() -> new ResourceNotFoundException("Land not found with id: " + landId));

        Transaction transaction = land.getTransactionHistory().stream()
                .filter(t -> transactionId.equals(t.getId()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found in land with id: " + transactionId));

        transaction.setStatus(status);

        // If transaction is completed, transfer ownership
        if ("COMPLETED".equals(status)) {
            land.setCurrentOwnerId(transaction.getTo()); // Use getTo

            // Update user's owned lands
            if (transaction.getFrom() != null) { // Use getFrom
                userRepository.findById(transaction.getFrom()).ifPresent(fromUser -> { // Use getFrom
                    fromUser.getOwnedLands().removeIf(land.getSurveyNumber()::equals); // Use getSurveyNumber
                    userRepository.save(fromUser);
                });
            }

            if (transaction.getTo() != null) { // Use getTo
                userRepository.findById(transaction.getTo()).ifPresent(toUser -> { // Use getTo
                    if (!toUser.getOwnedLands().contains(land.getSurveyNumber())) { // Use getSurveyNumber
                        toUser.getOwnedLands().add(land.getSurveyNumber());
                        userRepository.save(toUser);
                    }
                });
            }
        }

        landRepository.save(land);

        return convertToDto(
                transaction,
                land,
                transaction.getFrom() != null ?
                        userRepository.findById(transaction.getFrom()).orElse(null) : null, // Use getFrom
                transaction.getTo() != null ?
                        userRepository.findById(transaction.getTo()).orElse(null) : null // Use getTo
        );
    }

    private TransactionDTO convertToDto(Transaction transaction, LandParcel land, User fromUser, User toUser) {
        TransactionDTO dto = modelMapper.map(transaction, TransactionDTO.class);
        if (transaction.getTransactionDate() != null) {
            dto.setTransactionDate(transaction.getTransactionDate().toString());
        }

        if (land != null) {
            dto.setLandId(land.getSurveyNumber()); // Use getSurveyNumber
            dto.setLandTitle(land.getTitle());
        }

        if (fromUser != null) {
            dto.setFromUserId(fromUser.getId());
            dto.setFromUserEmail(fromUser.getEmail());
        }

        if (toUser != null) {
            dto.setToUserId(toUser.getId());
            dto.setToUserEmail(toUser.getEmail());
        }

        return dto;
    }

    private String generateTransactionId() {
        return "TXN-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 1000);
    }
}