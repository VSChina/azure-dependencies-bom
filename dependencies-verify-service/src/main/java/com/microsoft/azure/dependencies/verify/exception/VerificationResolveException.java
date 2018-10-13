package com.microsoft.azure.dependencies.verify.exception;

public class VerificationResolveException extends RuntimeException {

    public VerificationResolveException(String message) {
        super(message);
    }

    public VerificationResolveException(String message, Throwable e) {
        super(message, e);
    }
}
