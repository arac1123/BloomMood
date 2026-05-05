package com.bloommood.dto;

public class UserViewDto {
    private Long uid;
    private String email;
    private String uname;
    private String role;

    public UserViewDto(Long uid, String email, String uname, String role) {
        this.uid = uid;
        this.email = email;
        this.uname = uname;
        this.role = role;
    }

    public Long getUid() { return uid; }
    public String getEmail() { return email; }
    public String getUname() { return uname; }
    public String getRole() { return role; }
}
