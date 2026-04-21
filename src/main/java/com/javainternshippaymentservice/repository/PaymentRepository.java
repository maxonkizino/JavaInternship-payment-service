package com.javainternshippaymentservice.repository;

import com.javainternshippaymentservice.model.Payment;
import com.javainternshippaymentservice.model.PaymentStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * MongoDB repository for {@link Payment}. Finder methods exclude one status (typically {@link PaymentStatus#CANCELLED})
 * so cancelled documents stay in the collection but are hidden from normal reads.
 */
public interface PaymentRepository extends MongoRepository<Payment, UUID> {

    /**
     * Finds a non-cancelled payment for the order.
     *
     * @param orderId        order id
     * @param excludedStatus status to exclude (e.g. {@link PaymentStatus#CANCELLED})
     */
    @Query("{ 'orderId': ?0, 'status': { $ne: ?1 } }")
    Optional<Payment> findActiveByOrderId(UUID orderId, PaymentStatus excludedStatus);

    /**
     * Finds a non-cancelled payment by id.
     *
     * @param id             payment id
     * @param excludedStatus status to exclude (e.g. {@link PaymentStatus#CANCELLED})
     */
    @Query("{ '_id': ?0, 'status': { $ne: ?1 } }")
    Optional<Payment> findActiveById(UUID id, PaymentStatus excludedStatus);

    /**
     * Lists all payments that do not have the excluded status.
     *
     * @param excludedStatus status to exclude (e.g. {@link PaymentStatus#CANCELLED})
     */
    @Query("{ 'status': { $ne: ?0 } }")
    List<Payment> findAllActive(PaymentStatus excludedStatus);
}
