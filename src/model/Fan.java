package model;

public class Fan extends User {

    private String dateOfBirth;
    private String identityCard;
    private String address;

    public Fan(String userId, String fullName, String email,
            String passwordHash, String phone, String role,
            boolean isActive, String dateOfBirth,
            String identityCard, String address) {

        super(userId, fullName, email, passwordHash,
                phone, role, isActive);

        this.dateOfBirth = dateOfBirth;
        this.identityCard = identityCard;
        this.address = address;
    }

    public boolean register() {
        return true;
    }

    public void viewPurchasedTickets() {
        System.out.println("Purchased tickets.");
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public String getIdentityCard() {
        return identityCard;
    }

    public String getAddress() {
        return address;
    }
}