package com.javainternshippaymentservice.model.specification;

import com.javainternshippaymentservice.model.PaymentStatus;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.query.Criteria;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaymentSpecificationTest {

    @Test
    void hasId_shouldReturnNullForNullValue() {
        assertNull(PaymentSpecification.hasId(null));
    }

    @Test
    void hasId_shouldCreateCriteriaForProvidedValue() {
        Criteria criteria = PaymentSpecification.hasId(UUID.randomUUID());
        assertNotNull(criteria);
        assertTrue(criteria.getCriteriaObject().containsKey("_id"));
    }

    @Test
    void hasStatusesIn_shouldReturnNullForEmptyList() {
        assertNull(PaymentSpecification.hasStatusesIn(List.of()));
    }

    @Test
    void createdAtBetween_shouldSupportOnlyLowerBound() {
        Criteria criteria = PaymentSpecification.createdAtBetween(Instant.now(), null);
        assertNotNull(criteria);
        assertTrue(criteria.getCriteriaObject().containsKey("createdAt"));
    }

    @Test
    void createdAtBetween_shouldSupportOnlyUpperBound() {
        Criteria criteria = PaymentSpecification.createdAtBetween(null, Instant.now());
        assertNotNull(criteria);
        assertTrue(criteria.getCriteriaObject().containsKey("createdAt"));
    }

    @Test
    void allOf_shouldReturnEmptyCriteriaForNoParts() {
        Criteria criteria = PaymentSpecification.allOf();
        assertNotNull(criteria);
        assertTrue(criteria.getCriteriaObject().isEmpty());
    }

    @Test
    void allOf_shouldCombineMultipleCriteria() {
        Criteria criteria = PaymentSpecification.allOf(
                PaymentSpecification.hasStatus(PaymentStatus.CREATED),
                PaymentSpecification.hasUserId(10L)
        );
        assertNotNull(criteria);
        assertTrue(criteria.getCriteriaObject().containsKey("$and"));
    }
}
