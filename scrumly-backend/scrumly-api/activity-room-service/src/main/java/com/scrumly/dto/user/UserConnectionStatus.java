package com.scrumly.dto.user;

public enum UserConnectionStatus {
    CONNECTED("CONNECTED"),
    DISCONNECTED("DISCONNECTED"),
    KICKED("KICKED");

    private final String status;

    UserConnectionStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
