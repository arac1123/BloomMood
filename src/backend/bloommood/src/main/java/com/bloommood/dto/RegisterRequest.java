package com.bloommood.dto;

public class RegisterRequest {
    private String email;
    private String uname;
    private String upassword;

    public RegisterRequest() {}

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getUname() { return uname; }
    public void setUname(String uname) { this.uname = uname; }

    public String getPassword() { return upassword; }
    public void setPassword(String password) { this.upassword = password; }
}
