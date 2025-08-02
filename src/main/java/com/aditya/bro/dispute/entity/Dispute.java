package com.aditya.bro.dispute.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(collection = "disputes")
public class Dispute {
    @Id
    private String id;
    private String surveyNumber;
    private String raisedBy; // userId or wallet
    private String reason;
    private List<String> evidenceUrls; // case documents
    private boolean active;
    private LocalDateTime raisedAt;
    private LocalDateTime resolvedAt;

    public void setSurveyNumber(String surveyNumber) {
        this.surveyNumber = surveyNumber;
    }

    public void setRaisedBy(String raisedBy) {
        this.raisedBy = raisedBy;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public void setEvidenceUrls(List<String> evidenceUrls) {
        this.evidenceUrls = evidenceUrls;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setRaisedAt(LocalDateTime raisedAt) {
        this.raisedAt = raisedAt;
    }

    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }
}
