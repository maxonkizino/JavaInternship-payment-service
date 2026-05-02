package com.javainternshippaymentservice.repository;

import com.javainternshippaymentservice.model.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.Criteria;

/**
 * Custom persistence operations for {@link Payment} not expressible as derived or {@code @Query} methods.
 */
public interface PaymentRepositoryCustom {

    Page<Payment> findPageByCriteria(Criteria criteria, Pageable pageable);
}
