package com.aditya.bro.land.repository;

import com.aditya.bro.land.entity.LandParcel;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

import java.util.Optional;

public interface LandRepository extends MongoRepository<LandParcel, String> {
    Optional<LandParcel> findBySurveyNumber(String surveyNumber);
    List<LandParcel> findByCurrentOwnerId(String currentOwnerId);
    List<LandParcel> findByLocation(String location);
    List<LandParcel> findByStatus(String status);
    List<LandParcel> findByLocationContainingIgnoreCaseAndSurveyNumberContainingIgnoreCase(String location, String surveyNumber);
    List<LandParcel> findByLocationContainingIgnoreCase(String location);
    List<LandParcel> findBySurveyNumberContainingIgnoreCase(String surveyNumber);
}