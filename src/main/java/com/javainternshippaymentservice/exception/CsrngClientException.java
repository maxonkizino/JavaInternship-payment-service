package com.javainternshippaymentservice.exception;

public class CsrngClientException extends RuntimeException {

    public CsrngClientException(String message) {
        super(message);
    }

    public CsrngClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
