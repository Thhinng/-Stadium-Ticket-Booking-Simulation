package model;

public class Admin extends User {

    private int adminLevel;

    public Admin(String userId, String fullName, String email,
            String passwordHash, String phone, String role,
            boolean isActive, int adminLevel) {

        super(userId, fullName, email, passwordHash,
                phone, role, isActive);

        this.adminLevel = adminLevel;
    }

    public void lockUser(String userId) {
        System.out.println("User " + userId + " has been locked.");
    }

    public void unlockUser(String userId) {
        System.out.println("User " + userId + " has been unlocked.");
    }

    public int getAdminLevel() {
        return adminLevel;
    }
}