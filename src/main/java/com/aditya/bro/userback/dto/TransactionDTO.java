package com.aditya.bro.userback.dto;

import lombok.Data;

@Data
public class TransactionDTO {
    private String id;
    private String landId;
    private String landTitle;

    private String fromUserId;
    private String fromUserEmail;

    private String toUserId;
    private String toUserEmail;

    private String transactionDate;
    private String type;
    private double amount;
    private String status;
    private String notes;

    public String getLandId() {
        return landId;
    }

    public String getFromUserId() {
        return fromUserId;
    }

    public void setToUserEmail(String toUserEmail) {
        this.toUserEmail = toUserEmail;
    }

    public String getType() {
        return type;
    }

    public double getAmount() {
        return amount;
    }

    public String getNotes() {
        return notes;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setLandId(String landId) {
        this.landId = landId;
    }

    public String getLandTitle() {
        return landTitle;
    }

    public void setLandTitle(String landTitle) {
        this.landTitle = landTitle;
    }

    public void setFromUserId(String fromUserId) {
        this.fromUserId = fromUserId;
    }

    public String getFromUserEmail() {
        return fromUserEmail;
    }

    public void setFromUserEmail(String fromUserEmail) {
        this.fromUserEmail = fromUserEmail;
    }

    public void setToUserId(String toUserId) {
        this.toUserId = toUserId;
    }

    public void setToUserEmail(String toUserEmail) {
        this.toUserEmail = toUserEmail;
    }

    public String getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(String transactionDate) {
        this.transactionDate = transactionDate;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
