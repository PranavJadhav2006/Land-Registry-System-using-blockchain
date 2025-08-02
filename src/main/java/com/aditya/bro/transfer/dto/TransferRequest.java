package com.aditya.bro.transfer.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TransferRequest {

    @NotBlank(message = "Survey number is required")
    private String surveyNumber;

    private String fromWallet; // This will be set from the authenticated user

    @NotBlank(message = "Recipient wallet address is required")
    private String toWallet;

    @NotBlank(message = "A reason for the transfer is required")
    private String reason;

    private String remarks; // Optional remarks

    public String getSurveyNumber() {
        return surveyNumber;
    }

    public void setSurveyNumber(String surveyNumber) {
        this.surveyNumber = surveyNumber;
    }

    public String getFromWallet() {
        return fromWallet;
    }

    public void setFromWallet(String fromWallet) {
        this.fromWallet = fromWallet;
    }

    public String getToWallet() {
        return toWallet;
    }

    public void setToWallet(String toWallet) {
        this.toWallet = toWallet;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
