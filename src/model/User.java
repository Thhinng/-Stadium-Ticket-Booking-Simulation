package model;

public class User {
    public int id;
    public Role role;
    public String username;
    public String name;
    public String salt;
    public String passwordHash;
    public boolean active;
    public long createdAt;

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
}
