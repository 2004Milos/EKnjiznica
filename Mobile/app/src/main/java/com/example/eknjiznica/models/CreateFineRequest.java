package com.example.eknjiznica.models;

import java.math.BigDecimal;

public class CreateFineRequest {
    private String userId;
    private double amount;
    private String reason;

    public CreateFineRequest() {
    }

    public CreateFineRequest(String userId, double amount, String reason) {
        this.userId = userId;
        this.amount = amount;
        this.reason = reason;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
