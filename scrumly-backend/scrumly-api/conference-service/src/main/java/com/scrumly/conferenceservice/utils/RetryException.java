package com.scrumly.conferenceservice.utils;

public class RetryException extends Exception {
    public RetryException(String message) {
        super(message);
    }

    public RetryException(String message, Throwable cause) {
        super(message, cause);
    }
}
