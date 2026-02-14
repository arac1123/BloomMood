package com.bloommood.dto;

/**
 * Frontend obtains an authorization code from Google OAuth (GIS initCodeClient)
 * and sends it to backend for server-side token exchange.
 */
public class GoogleAuthCodeRequest {

    private String code;

    public GoogleAuthCodeRequest() {
    }

    public GoogleAuthCodeRequest(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}

