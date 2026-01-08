package com.example.eknjiznica.models;

public class CreateLoanRequest {
    private int bookId;
    private String userId;
    private int days;

    public CreateLoanRequest() {
    }

    public CreateLoanRequest(int bookId, String userId, int days) {
        this.bookId = bookId;
        this.userId = userId;
        this.days = days;
    }

    public int getBookId() {
        return bookId;
    }

    public void setBookId(int bookId) {
        this.bookId = bookId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getDays() {
        return days;
    }

    public void setDays(int days) {
        this.days = days;
    }
}
