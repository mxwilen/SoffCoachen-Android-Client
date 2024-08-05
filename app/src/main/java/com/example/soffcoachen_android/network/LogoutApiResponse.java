package com.example.soffcoachen_android.network;

public class LogoutApiResponse {
    private String status;

    public LogoutApiResponse(String status) {
        this.status = status;
    }

    public String getStatus() {
        return this.status;
    }
}
