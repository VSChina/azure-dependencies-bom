package com.microsoft.azure.dependencies.verify.exception;

public class VerificationDependencyConflictException extends RuntimeException {

    public VerificationDependencyConflictException(String message) {
        super(message);
    }

    public VerificationDependencyConflictException(String message, Throwable e) {
        super(message, e);
    }
}
