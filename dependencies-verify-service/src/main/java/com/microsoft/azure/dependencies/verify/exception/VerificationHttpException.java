package com.microsoft.azure.dependencies.verify.exception;

public class VerificationHttpException extends RuntimeException {

    public VerificationHttpException(Throwable e) {
        super(e);
    }

    public VerificationHttpException(String message, Throwable e) {
        super(message, e);
    }
}
