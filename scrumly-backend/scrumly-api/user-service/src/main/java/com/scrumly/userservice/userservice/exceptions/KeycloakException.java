package com.scrumly.userservice.userservice.exceptions;

public class KeycloakException extends RuntimeException {
    public KeycloakException() {
        super();
    }

    public KeycloakException(String message) {
        super(message);
    }

    public KeycloakException(String message, Throwable cause) {
        super(message, cause);
    }

    public KeycloakException(Throwable cause) {
        super(cause);
    }

    protected KeycloakException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
