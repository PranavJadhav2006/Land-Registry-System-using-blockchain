package com.aditya.bro.transfer.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "ownership_transfers")
public class OwnershipTransfer {
    @Id
    private String id;

    private String surveyNumber;
    private String fromWallet;
    private String toWallet;
    private String status; // INITIATED, CONFIRMED, FAILED
    private String remarks;
    private String reason;
    private LocalDateTime initiatedAt;
    private LocalDateTime confirmedAt;

    public void setSurveyNumber(String surveyNumber) {
        this.surveyNumber = surveyNumber;
    }

    public void setFromWallet(String fromWallet) {
        this.fromWallet = fromWallet;
    }

    public void setToWallet(String toWallet) {
        this.toWallet = toWallet;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public void setInitiatedAt(LocalDateTime initiatedAt) {
        this.initiatedAt = initiatedAt;
    }

    public void setConfirmedAt(LocalDateTime confirmedAt) {
        this.confirmedAt = confirmedAt;
    }

    public String getStatus() {
        return status;
    }

    public String getFromWallet() {
        return fromWallet;
    }

    public String getToWallet() {
        return toWallet;
    }

    public String getSurveyNumber() {
        return surveyNumber;
    }

    public String getReason() {
        return reason;
    }

    public String getRemarks() {
        return remarks;
    }
}
