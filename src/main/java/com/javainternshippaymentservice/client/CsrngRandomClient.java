package com.javainternshippaymentservice.client;

/**
 * Client for CSRNG Lite random number API ({@code https://csrng.net/csrng/csrng.php}).
 */
public interface CsrngRandomClient {

    /**
     * Requests one random integer in the configured inclusive range.
     *
     * @return the {@code random} field from a successful CSRNG response
     */
    long fetchRandomInclusive();
}
