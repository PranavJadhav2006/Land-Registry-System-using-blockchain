package com.aditya.bro.userback.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import java.time.LocalDateTime;

@Data
public class Transaction {
    @Id
    private String id;

    private String landId;
    private String fromUserId;
    private String toUserId;

    private LocalDateTime transactionDate;
    private String type;
    private double amount;
    private String status;
    private String notes;

    public void setId(String id) {
        this.id = id;
    }

    public void setTransactionDate(LocalDateTime transactionDate) {
        this.transactionDate = transactionDate;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getLandId() {
        return landId;
    }

    public String getFromUserId() {
        return fromUserId;
    }

    public String getToUserId() {
        return toUserId;
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
}
