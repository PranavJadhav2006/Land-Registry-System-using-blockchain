package com.aditya.bro.userback.dto;

import lombok.Data;
import java.util.List;

@Data
public class LandDTO {
    private String id;
    private String surveyNumber;
    private String title;
    private String description;
    private String location;
    private double area;
    private double marketValue;
    private String currentOwnerEmail;
    private String registrationDate;
    private String status;
    private List<String> imageUrls;
    private List<com.aditya.bro.land.entity.LandParcel.Document> documents;
    private List<com.aditya.bro.land.entity.LandParcel.Transaction> transactionHistory;
    
}