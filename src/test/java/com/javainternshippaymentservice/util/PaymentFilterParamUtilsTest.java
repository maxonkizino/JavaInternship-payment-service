package com.javainternshippaymentservice.util;

import com.javainternshippaymentservice.model.PaymentStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaymentFilterParamUtilsTest {

    @Test
    void hasAnyFilter_shouldReturnFalseWhenAllAreNullOrEmpty() {
        boolean result = PaymentFilterParamUtils.hasAnyFilter(
                null,
                List.of(),
                null,
                null,
                null,
                null);

        assertFalse(result);
    }

    @Test
    void hasAnyFilter_shouldReturnTrueWhenAtLeastOneFilterExists() {
        boolean result = PaymentFilterParamUtils.hasAnyFilter(
                PaymentStatus.CREATED,
                null,
                Instant.now(),
                null,
                1L,
                UUID.randomUUID());

        assertTrue(result);
    }
}
