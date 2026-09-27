package com.stadium.model;

import java.io.Serializable;

/**
 * Basic User Model to support Booking & Payment module operations.
 */
public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    private String userId;
    private String username;
    private String fullName;
    private String email;
    private String phone;

    public User(String userId, String username, String fullName, String email, String phone) {
        this.userId = userId;
        this.username = username;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
    }

    public String getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
}
