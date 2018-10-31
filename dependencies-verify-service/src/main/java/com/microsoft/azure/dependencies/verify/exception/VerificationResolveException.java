/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.dependencies.verify.exception;

public class VerificationResolveException extends RuntimeException {

    public VerificationResolveException(String message) {
        super(message);
    }

    public VerificationResolveException(String message, Throwable e) {
        super(message, e);
    }
}
