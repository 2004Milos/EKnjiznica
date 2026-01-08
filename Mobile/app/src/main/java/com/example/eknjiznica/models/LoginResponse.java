package com.example.eknjiznica.models;

import java.util.List;

public class LoginResponse {
    private String token;
    private String email;
    private String userId;
    private List<String> roles;

    public LoginResponse() {
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public boolean isLibrarian() {
        return roles != null && roles.contains("Librarian");
    }

    public boolean isMember() {
        return roles != null && roles.contains("Member");
    }
}
