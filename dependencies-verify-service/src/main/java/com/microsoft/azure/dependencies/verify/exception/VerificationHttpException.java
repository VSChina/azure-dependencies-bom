package com.microsoft.azure.dependencies.verify.exception;

public class VerificationHttpException extends RuntimeException {

    public VerificationHttpException(String message) {
        super(message);
    }

    public VerificationHttpException(String message, Throwable e) {
        super(message, e);
    }
}
