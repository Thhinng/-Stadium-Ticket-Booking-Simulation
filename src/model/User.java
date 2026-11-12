package model;

public class User {
    private int id;
    private Role role;
    private String username;
    private String name;
    private String salt;
    private String passwordHash;
    private boolean active;
    private long createdAt;

    public User(int id, Role role, String username, String name, String salt, String passwordHash,
                boolean active, long createdAt) {
        this.id = id;
        this.role = role;
        this.username = username;
        this.name = name;
        this.salt = salt;
        this.passwordHash = passwordHash;
        this.active = active;
        this.createdAt = createdAt;
    }

    public int getId() { return this.id; }
    public Role getRole() { return this.role; }
    public String getUsername() { return this.username; }
    public String getName() { return this.name; }
    public void setName(String value) { this.name = value; }
    public String getSalt() { return this.salt; }
    public String getPasswordHash() { return this.passwordHash; }
    public boolean getActive() { return this.active; }
    public void setActive(boolean value) { this.active = value; }
    public long getCreatedAt() { return this.createdAt; }
    public void setPasswordCredentials(String salt, String passwordHash) {
        this.salt = salt;
        this.passwordHash = passwordHash;
    }
}
