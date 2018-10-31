/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.dependencies.verify.exception;

public class VerificationHttpException extends RuntimeException {

    public VerificationHttpException(String message) {
        super(message);
    }

    public VerificationHttpException(String message, Throwable e) {
        super(message, e);
    }
}
