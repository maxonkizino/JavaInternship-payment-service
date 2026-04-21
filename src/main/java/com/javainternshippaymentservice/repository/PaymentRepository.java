package com.javainternshippaymentservice.repository;

import com.javainternshippaymentservice.model.Payment;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends MongoRepository<Payment, UUID> {

    Optional<Payment> findByOrderId(UUID orderId);
}
