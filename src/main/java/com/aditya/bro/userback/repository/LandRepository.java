package com.aditya.bro.userback.repository;

import com.aditya.bro.land.entity.LandParcel; // Changed import
import com.aditya.bro.land.entity.LandParcel.Transaction; // Changed import
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;
import java.util.List;
import java.util.Optional;

public interface LandRepository extends MongoRepository<LandParcel, String> {

    // Basic land queries
    List<LandParcel> findByCurrentOwnerId(String ownerId);
    Optional<LandParcel> findBySurveyNumber(String surveyNumber);

    // Search functionality
    List<LandParcel> findByLocationContainingIgnoreCase(String location);
    List<LandParcel> findBySurveyNumberContainingIgnoreCase(String surveyNumber);
    List<LandParcel> findByLocationContainingIgnoreCaseAndSurveyNumberContainingIgnoreCase(String location, String surveyNumber);

    // Status-based queries
    List<LandParcel> findByStatus(String status);

    // Transaction-related queries
    @Query("{ $or: [ " +
            "{ 'currentOwnerId': ?0 }, " +
            "{ 'transactionHistory.from': ?0 }, " + // Changed from fromUserId
            "{ 'transactionHistory.to': ?0 } " + // Changed from toUserId
            "] }")
    List<LandParcel> findLandsByUserInvolvement(String userId);

    @Query("{ 'surveyNumber': ?0 }") // Changed from _id
    @Update("{ $push: { 'transactionHistory': ?1 } }")
    void addTransaction(String surveyNumber, Transaction transaction);

    @Query("{ 'surveyNumber': ?0, 'transactionHistory.id': ?1 }") // Changed from _id and _id
    @Update("{ $set: { 'transactionHistory.$.status': ?2 } }")
    void updateTransactionStatus(String surveyNumber, String transactionId, String status);
}