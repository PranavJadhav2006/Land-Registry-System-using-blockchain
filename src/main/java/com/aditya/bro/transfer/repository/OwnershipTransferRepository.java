package com.aditya.bro.transfer.repository;

import com.aditya.bro.transfer.entity.OwnershipTransfer;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface OwnershipTransferRepository extends MongoRepository<OwnershipTransfer, String> {
    Optional<OwnershipTransfer> findBySurveyNumberAndStatus(String surveyNumber, String status);
    List<OwnershipTransfer> findBySurveyNumberOrderByInitiatedAtDesc(String surveyNumber);
    Optional<OwnershipTransfer> findTopBySurveyNumberOrderByInitiatedAtDesc(String surveyNumber);
}
