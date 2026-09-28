
package model;

public class User {
    private String userId;
    private String fullName;
    private String email;
    private String passwordHash;
    private String phone;
    private String role;
    private boolean isActive;
    
    public User(String userId, String fullName, String emaill, String  passwordHash, String phone, String role, boolean isActive) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.phone = phone;
        this.role = role;
        this.isActive = isActive;
    }
    public boolean login (String email, String password) {
        if(this.email.equals(email) && this.passwordHash.equals(password) && this.isActive) {
            return true;
        }
        return false;
    }
    public void logout() {
        System.out.println("Logout successfully!");
    }
    public void updateProfile() {
        System.out.println("Profile updated! ");
        
    }
    public String getUserId() {
        return userId;
    }
    public String getFullName() {
        return fullName;
    }
    public String getEmail() {
        return email;
    }
    public String getPasswordHash() {
        return passwordHash;
    }
    public String getPhone() {
        return phone;
    }
    public String getRole() {
        return role;
    }
    public boolean isActive() {
        return isActive;
    }
    public void setActive(boolean active) {
        isActive = active;
    }
}
