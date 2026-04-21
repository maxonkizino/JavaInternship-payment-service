package com.javainternshippaymentservice.repository;

import com.javainternshippaymentservice.model.Payment;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

@RequiredArgsConstructor
public class PaymentRepositoryImpl implements PaymentRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    @Override
    public Page<Payment> findPageByCriteria(Criteria criteria, Pageable pageable) {
        Criteria effective = criteria == null ? new Criteria() : criteria;
        Query query = Query.query(effective).with(pageable);
        List<Payment> content = mongoTemplate.find(query, Payment.class);
        long total = mongoTemplate.count(Query.query(effective), Payment.class);
        return new PageImpl<>(content, pageable, total);
    }
}
