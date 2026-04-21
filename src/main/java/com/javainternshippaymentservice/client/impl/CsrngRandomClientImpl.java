package com.javainternshippaymentservice.client.impl;

import com.javainternshippaymentservice.client.CsrngRandomClient;
import com.javainternshippaymentservice.client.dto.CsrngLiteResponseEntry;
import com.javainternshippaymentservice.exception.CsrngClientException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;


@Component
public class CsrngRandomClientImpl implements CsrngRandomClient {

    private final RestClient csrngRestClient;
    private final String apiPath;
    private final long minInclusive;
    private final long maxInclusive;

    public CsrngRandomClientImpl(
            @Qualifier("csrngRestClient") RestClient csrngRestClient,
            @Value("${app.csrng.path}") String apiPath,
            @Value("${app.csrng.min}") long minInclusive,
            @Value("${app.csrng.max}") long maxInclusive) {
        this.csrngRestClient = csrngRestClient;
        this.apiPath = apiPath;
        this.minInclusive = minInclusive;
        this.maxInclusive = maxInclusive;
    }

    @Override
    public long fetchRandomInclusive() {
        if (minInclusive > maxInclusive) {
            throw new CsrngClientException("Invalid CSRNG range: min > max");
        }
        try {
            CsrngLiteResponseEntry[] body = csrngRestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(apiPath)
                            .queryParam("min", minInclusive)
                            .queryParam("max", maxInclusive)
                            .build())
                    .retrieve()
                    .body(CsrngLiteResponseEntry[].class);
            CsrngLiteResponseEntry entry = body != null && body.length > 0 ? body[0] : null;
            if (entry == null) {
                throw new CsrngClientException("Empty CSRNG response body");
            }
            if (!"success".equalsIgnoreCase(entry.getStatus())) {
                String detail = entry.getReason() != null ? entry.getReason()
                        : ("CSRNG error" + (entry.getCode() != null ? " code=" + entry.getCode() : ""));
                throw new CsrngClientException(detail);
            }
            if (entry.getRandom() == null) {
                throw new CsrngClientException("CSRNG response missing random");
            }
            return entry.getRandom();
        } catch (ResourceAccessException ex) {
            throw new CsrngClientException("CSRNG service unreachable", ex);
        } catch (RestClientResponseException ex) {
            throw new CsrngClientException("CSRNG HTTP error: " + ex.getStatusCode(), ex);
        }
    }
}
