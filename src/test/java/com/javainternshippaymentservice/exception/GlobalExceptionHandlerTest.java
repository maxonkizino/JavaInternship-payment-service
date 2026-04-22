package com.javainternshippaymentservice.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;

import java.nio.charset.StandardCharsets;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handlePaymentNotFound_shouldReturn404() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/payments/1");
        ResponseEntity<ApiError> response = handler.handlePaymentNotFound(
                new PaymentNotFoundException("not found"),
                request
        );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("not found", response.getBody().getMessage());
    }

    @Test
    void handlePaymentCardRule_shouldReturn400() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/payments");
        ResponseEntity<ApiError> response = handler.handlePaymentCardRule(
                new PaymentCardOwnershipException("card invalid"),
                request
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("card invalid", response.getBody().getMessage());
    }

    @Test
    void handleConstraintViolation_shouldReturn400WithViolationText() {
        @SuppressWarnings("unchecked")
        ConstraintViolation<Object> violation = (ConstraintViolation<Object>) mock(ConstraintViolation.class);
        Path path = mock(Path.class);
        when(path.toString()).thenReturn("paymentCardId");
        when(violation.getPropertyPath()).thenReturn(path);
        when(violation.getMessage()).thenReturn("must be positive");
        ConstraintViolationException ex = new ConstraintViolationException(Set.of(violation));

        MockHttpServletRequest request = new MockHttpServletRequest("PUT", "/api/payments/1");
        ResponseEntity<ApiError> response = handler.handleConstraintViolation(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().getMessage().contains("paymentCardId"));
    }

    @Test
    void handleRestClient_shouldMapServerErrorToBadGateway() {
        RestClientResponseException ex = new RestClientResponseException(
                "downstream fail",
                500,
                "Internal Server Error",
                null,
                "service fail".getBytes(StandardCharsets.UTF_8),
                StandardCharsets.UTF_8
        );
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/payments");
        ResponseEntity<ApiError> response = handler.handleRestClient(ex, request);

        assertEquals(HttpStatus.BAD_GATEWAY, response.getStatusCode());
        assertEquals("service fail", response.getBody().getMessage());
    }

    @Test
    void handleResourceAccess_shouldReturn503() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/payments");
        ResponseEntity<ApiError> response = handler.handleResourceAccess(
                new ResourceAccessException("timeout"),
                request
        );

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertEquals("Remote service temporarily unavailable", response.getBody().getMessage());
    }

    @Test
    void handleGeneric_shouldReturn500() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/payments");
        ResponseEntity<ApiError> response = handler.handleGeneric(new RuntimeException("boom"), request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("An unexpected error occurred", response.getBody().getMessage());
    }

    @Test
    void handleDuplicateKey_shouldReturn409() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/payments");
        ResponseEntity<ApiError> response = handler.handleDuplicateKey(
                new DuplicateKeyException("duplicate"),
                request
        );

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Duplicate resource", response.getBody().getMessage());
    }

    @Test
    void handleCsrng_shouldReturn502() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/payments");
        ResponseEntity<ApiError> response = handler.handleCsrng(
                new CsrngClientException("bad gateway"),
                request
        );

        assertEquals(HttpStatus.BAD_GATEWAY, response.getStatusCode());
        assertEquals("bad gateway", response.getBody().getMessage());
    }

    @Test
    void handleMissingParam_shouldReturn400() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/payments");
        ResponseEntity<ApiError> response = handler.handleMissingParam(
                new MissingServletRequestParameterException("paymentCardId", "Long"),
                request
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().getMessage().contains("paymentCardId"));
    }

    @Test
    void handleTypeMismatch_shouldReturn400WithParameterName() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/payments");
        MethodArgumentTypeMismatchException ex = new MethodArgumentTypeMismatchException(
                "abc", Long.class, "paymentCardId", null, null
        );

        ResponseEntity<ApiError> response = handler.handleTypeMismatch(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid parameter: paymentCardId", response.getBody().getMessage());
    }
}
