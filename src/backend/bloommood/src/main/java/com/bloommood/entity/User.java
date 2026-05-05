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
    // LOCAL 必填；GOOGLE 可為 null
    @Column(length = 100)
    private String password;

    @Column(nullable = false, length = 32)
    private String role = "ROLE_USER";

    /** Login provider: LOCAL / GOOGLE */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private AuthProvider authProvider = AuthProvider.LOCAL;

    /** Google id_token 的 sub（唯一且穩定） */
    @Column(length = 128, unique = true)
    private String oauthSub;

    public User() {}

    public User(String email, String uname, String password, String role) {
        this.email = email;
        this.uname = uname;
        this.password = password;
        this.role = role;
    }

    public User(String email, String uname, String password, String role, AuthProvider authProvider, String oauthSub) {
        this.email = email;
        this.uname = uname;
        this.password = password;
        this.role = role;
        this.authProvider = authProvider;
        this.oauthSub = oauthSub;
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

    public AuthProvider getAuthProvider() { return authProvider; }
    public void setAuthProvider(AuthProvider authProvider) { this.authProvider = authProvider; }

    public String getOauthSub() { return oauthSub; }
    public void setOauthSub(String oauthSub) { this.oauthSub = oauthSub; }
}
