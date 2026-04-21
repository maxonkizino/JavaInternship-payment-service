package com.javainternshippaymentservice.repository;

import com.javainternshippaymentservice.model.Payment;
import com.javainternshippaymentservice.model.PaymentStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Optional;
import java.util.UUID;

/**
 * MongoDB repository for {@link com.javainternshippaymentservice.model.Payment}.
 * Dynamic paged queries use {@link PaymentRepositoryCustom#findPageByCriteria}; the service composes
 * {@link org.springframework.data.mongodb.core.query.Criteria} via {@link com.javainternshippaymentservice.model.specification.PaymentSpecification}.
 */
public interface PaymentRepository extends MongoRepository<Payment, UUID>, PaymentRepositoryCustom {

    @Query("{ '_id': ?0, 'status': { $ne: ?1 } }")
    Optional<Payment> findActiveById(UUID id, PaymentStatus excludedStatus);

    @Query("{ 'orderId': ?0, 'status': { $ne: ?1 } }")
    Optional<Payment> findActiveByOrderId(UUID orderId, PaymentStatus excludedStatus);
}
