package com.bloommood.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long uid;

    @Column(nullable = false, unique = true, length = 128)
    private String email;

    @Column(nullable = false, length = 64)
    private String uname;

    // ✅ 存 hash（BCrypt 後的結果），不是明文
    @Column(nullable = false, length = 100)
    private String password;

    @Column(nullable = false, length = 32)
    private String role = "ROLE_USER";

    public User() {}

    public User(String email, String uname, String password, String role) {
        this.email = email;
        this.uname = uname;
        this.password = password;
        this.role = role;
    }

    public Long getUid() { return uid; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getUname() { return uname; }
    public void setUname(String uname) { this.uname = uname; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
