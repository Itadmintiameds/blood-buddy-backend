package com.bloodbuddy.exceptionhandling;


public class PasswordResetException extends RuntimeException {

    public PasswordResetException(String message) {
        super(message);
    }
}
