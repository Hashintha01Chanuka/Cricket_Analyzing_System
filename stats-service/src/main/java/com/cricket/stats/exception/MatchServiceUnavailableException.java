package com.cricket.stats.exception;

public class MatchServiceUnavailableException extends RuntimeException {

    public MatchServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
