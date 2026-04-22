package com.javainternshippaymentservice.repository;

import com.javainternshippaymentservice.model.Payment;
import com.javainternshippaymentservice.model.PaymentStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
class PaymentRepositoryIntegrationTest {

    @Container
    static final MongoDBContainer MONGO = new MongoDBContainer("mongo:7.0");

    @DynamicPropertySource
    static void mongoProps(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", () -> MONGO.getReplicaSetUrl("javainternship-payment-service-test"));
    }

    @Autowired
    private PaymentRepository paymentRepository;

    @AfterEach
    void cleanup() {
        paymentRepository.deleteAll();
    }

    @Test
    void findActiveById_shouldExcludeCancelled() {
        Payment active = savePayment(PaymentStatus.CREATED);
        Payment cancelled = savePayment(PaymentStatus.CANCELLED);

        Optional<Payment> activeFound = paymentRepository.findActiveById(active.getId(), PaymentStatus.CANCELLED);
        Optional<Payment> cancelledFound = paymentRepository.findActiveById(cancelled.getId(), PaymentStatus.CANCELLED);

        assertTrue(activeFound.isPresent());
        assertTrue(cancelledFound.isEmpty());
    }

    @Test
    void findActiveByOrderId_shouldExcludeCancelled() {
        UUID sharedOrderId = UUID.randomUUID();
        Payment cancelled = payment(sharedOrderId, PaymentStatus.CANCELLED);
        Payment active = payment(sharedOrderId, PaymentStatus.PROCESSING);
        paymentRepository.save(cancelled);
        paymentRepository.save(active);

        Optional<Payment> found = paymentRepository.findActiveByOrderId(sharedOrderId, PaymentStatus.CANCELLED);

        assertTrue(found.isPresent());
        assertEquals(PaymentStatus.PROCESSING, found.get().getStatus());
    }

    @Test
    void findPageByCriteria_shouldReturnPagedFilteredResults() {
        savePayment(PaymentStatus.CREATED);
        savePayment(PaymentStatus.PROCESSING);
        savePayment(PaymentStatus.SUCCEEDED);
        savePayment(PaymentStatus.CANCELLED);

        Criteria criteria = Criteria.where("status").ne(PaymentStatus.CANCELLED);
        Page<Payment> page = paymentRepository.findPageByCriteria(criteria, PageRequest.of(0, 2));

        assertEquals(2, page.getContent().size());
        assertEquals(3, page.getTotalElements());
        assertNotNull(page.getContent().getFirst().getId());
    }

    private Payment savePayment(PaymentStatus status) {
        return paymentRepository.save(payment(UUID.randomUUID(), status));
    }

    private static Payment payment(UUID orderId, PaymentStatus status) {
        Payment payment = new Payment();
        payment.setId(UUID.randomUUID());
        payment.setOrderId(orderId);
        payment.setUserId(1L);
        payment.setStatus(status);
        payment.setTimestamp(Instant.now());
        payment.setPaymentAmount(BigDecimal.TEN);
        return payment;
    }
}
