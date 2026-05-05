package com.bloommood.dto;

public class UpdateRequest {
    private String email;
    private String uname;
    private String oldPassword;
    private String newPassword;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getUname() { return uname; }
    public void setUname(String uname) { this.uname = uname; }

    public String getOldPassword() { return oldPassword; }
    public void setOldPassword(String oldPassword) { this.oldPassword = oldPassword; }

    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
}
