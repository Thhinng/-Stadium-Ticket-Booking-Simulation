package model;

public class Fan extends User {

    private String dateOfBirth;
    private String identityCard;
    private String address;

    public Fan() {
        super("", "", "", "", "", "FAN", true);
        this.dateOfBirth = "";
        this.identityCard = "";
        this.address = "";
    }

    public Fan(String id, String fullName, String email, String password, String phone, String status) {
        super(id, fullName, email, password, phone, "FAN", !"INACTIVE".equalsIgnoreCase(status));
        this.dateOfBirth = "";
        this.identityCard = "";
        this.address = "";
    }

    public Fan(String id, String fullName, String email, String password, String phone,
               String role, String status, String dateOfBirth, String identityCard, String address) {
        super(id, fullName, email, password, phone, role != null ? role : "FAN", !"INACTIVE".equalsIgnoreCase(status));
        this.dateOfBirth = dateOfBirth != null ? dateOfBirth : "";
        this.identityCard = identityCard != null ? identityCard : "";
        this.address = address != null ? address : "";
    }

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

    public String getId() {
        return getUserId();
    }

    public String getPassword() {
        return getPasswordHash();
    }

    public String getStatus() {
        return isActive() ? "ACTIVE" : "INACTIVE";
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
