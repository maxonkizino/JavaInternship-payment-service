package com.javainternshippaymentservice.exception;

/**
 * Thrown when CSRNG Lite API returns an error payload or the call cannot be completed as expected.
 */
public class CsrngClientException extends RuntimeException {

    public CsrngClientException(String message) {
        super(message);
    }

    public CsrngClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
