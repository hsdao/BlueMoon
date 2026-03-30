package models;

import java.time.LocalDateTime;

public class User {
    private int id;
    private String username;
    private String password;
    private String role;
    private String fullName;
    private LocalDateTime createdAt;

    public User() {}
    public User(int id, String username, String password, String role, String fullName, LocalDateTime createdAt) {
        this.id = id; this.username = username; this.password = password;
        this.role = role; this.fullName = fullName; this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
