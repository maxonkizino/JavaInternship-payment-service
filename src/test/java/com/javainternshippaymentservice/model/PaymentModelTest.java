package com.javainternshippaymentservice.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.EnumSet;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaymentModelTest {

    @Test
    void payment_shouldStoreAllFieldsIncludingAuditing() {
        Payment payment = new Payment();
        UUID id = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        Instant now = Instant.now();
        Instant createdAt = now.minusSeconds(60);

        payment.setId(id);
        payment.setOrderId(orderId);
        payment.setUserId(42L);
        payment.setStatus(PaymentStatus.PROCESSING);
        payment.setTimestamp(now);
        payment.setPaymentAmount(BigDecimal.TEN);
        payment.setCreatedAt(createdAt);
        payment.setUpdatedAt(now);

        assertEquals(id, payment.getId());
        assertEquals(orderId, payment.getOrderId());
        assertEquals(42L, payment.getUserId());
        assertEquals(PaymentStatus.PROCESSING, payment.getStatus());
        assertEquals(now, payment.getTimestamp());
        assertEquals(BigDecimal.TEN, payment.getPaymentAmount());
        assertEquals(createdAt, payment.getCreatedAt());
        assertEquals(now, payment.getUpdatedAt());
    }

    @Test
    void paymentStatus_shouldContainExpectedLifecycleValues() {
        EnumSet<PaymentStatus> statuses = EnumSet.allOf(PaymentStatus.class);
        assertTrue(statuses.contains(PaymentStatus.CREATED));
        assertTrue(statuses.contains(PaymentStatus.PROCESSING));
        assertTrue(statuses.contains(PaymentStatus.SUCCEEDED));
        assertTrue(statuses.contains(PaymentStatus.FAILED));
        assertTrue(statuses.contains(PaymentStatus.CANCELLED));
    }
}
