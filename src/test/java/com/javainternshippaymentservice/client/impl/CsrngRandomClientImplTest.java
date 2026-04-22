package com.javainternshippaymentservice.client.impl;

import com.javainternshippaymentservice.client.dto.CsrngLiteResponseEntry;
import com.javainternshippaymentservice.exception.CsrngClientException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CsrngRandomClientImplTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private RestClient restClient;

    @Test
    void fetchRandomInclusive_shouldReturnRandomWhenSuccess() {
        CsrngLiteResponseEntry entry = new CsrngLiteResponseEntry();
        entry.setStatus("success");
        entry.setRandom(42L);
        when(restClient.get().uri(anyUriFunction()).retrieve().body(CsrngLiteResponseEntry[].class))
                .thenReturn(new CsrngLiteResponseEntry[]{entry});

        CsrngRandomClientImpl client = new CsrngRandomClientImpl(restClient, "/csrng/csrng.php", 0, 100);
        long result = client.fetchRandomInclusive();

        assertEquals(42L, result);
    }

    @Test
    void fetchRandomInclusive_shouldThrowWhenRangeInvalid() {
        CsrngRandomClientImpl client = new CsrngRandomClientImpl(restClient, "/csrng/csrng.php", 10, 1);
        CsrngClientException ex = assertThrows(CsrngClientException.class, client::fetchRandomInclusive);
        assertEquals("Invalid CSRNG range: min > max", ex.getMessage());
    }

    @Test
    void fetchRandomInclusive_shouldThrowWhenEmptyBody() {
        when(restClient.get().uri(anyUriFunction()).retrieve().body(CsrngLiteResponseEntry[].class))
                .thenReturn(new CsrngLiteResponseEntry[0]);

        CsrngRandomClientImpl client = new CsrngRandomClientImpl(restClient, "/csrng/csrng.php", 0, 10);
        CsrngClientException ex = assertThrows(CsrngClientException.class, client::fetchRandomInclusive);
        assertEquals("Empty CSRNG response body", ex.getMessage());
    }

    @Test
    void fetchRandomInclusive_shouldThrowWhenStatusErrorWithReason() {
        CsrngLiteResponseEntry entry = new CsrngLiteResponseEntry();
        entry.setStatus("error");
        entry.setReason("rate limited");
        when(restClient.get().uri(anyUriFunction()).retrieve().body(CsrngLiteResponseEntry[].class))
                .thenReturn(new CsrngLiteResponseEntry[]{entry});

        CsrngRandomClientImpl client = new CsrngRandomClientImpl(restClient, "/csrng/csrng.php", 0, 10);
        CsrngClientException ex = assertThrows(CsrngClientException.class, client::fetchRandomInclusive);
        assertEquals("rate limited", ex.getMessage());
    }

    @Test
    void fetchRandomInclusive_shouldThrowWhenRandomMissing() {
        CsrngLiteResponseEntry entry = new CsrngLiteResponseEntry();
        entry.setStatus("success");
        entry.setRandom(null);
        when(restClient.get().uri(anyUriFunction()).retrieve().body(CsrngLiteResponseEntry[].class))
                .thenReturn(new CsrngLiteResponseEntry[]{entry});

        CsrngRandomClientImpl client = new CsrngRandomClientImpl(restClient, "/csrng/csrng.php", 0, 10);
        CsrngClientException ex = assertThrows(CsrngClientException.class, client::fetchRandomInclusive);
        assertEquals("CSRNG response missing random", ex.getMessage());
    }

    @Test
    void fetchRandomInclusive_shouldWrapResourceAccess() {
        when(restClient.get().uri(anyUriFunction()).retrieve().body(CsrngLiteResponseEntry[].class))
                .thenThrow(new ResourceAccessException("timeout"));

        CsrngRandomClientImpl client = new CsrngRandomClientImpl(restClient, "/csrng/csrng.php", 0, 10);
        CsrngClientException ex = assertThrows(CsrngClientException.class, client::fetchRandomInclusive);
        assertEquals("CSRNG service unreachable", ex.getMessage());
    }

    @Test
    void fetchRandomInclusive_shouldWrapHttpError() {
        RestClientResponseException httpError = new RestClientResponseException(
                "bad gateway",
                502,
                "Bad Gateway",
                null,
                null,
                StandardCharsets.UTF_8
        );
        when(restClient.get().uri(anyUriFunction()).retrieve().body(CsrngLiteResponseEntry[].class))
                .thenThrow(httpError);

        CsrngRandomClientImpl client = new CsrngRandomClientImpl(restClient, "/csrng/csrng.php", 0, 10);
        CsrngClientException ex = assertThrows(CsrngClientException.class, client::fetchRandomInclusive);
        assertEquals("CSRNG HTTP error: 502 BAD_GATEWAY", ex.getMessage());
    }

    private static Function<UriBuilder, URI> anyUriFunction() {
        return any();
    }
}
