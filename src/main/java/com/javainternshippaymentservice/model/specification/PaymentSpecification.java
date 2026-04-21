package com.javainternshippaymentservice.model.specification;

import com.javainternshippaymentservice.model.PaymentStatus;
import org.springframework.data.mongodb.core.query.Criteria;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class PaymentSpecification {

    private static final String FIELD_ID = "_id";
    private static final String FIELD_ORDER_ID = "orderId";
    private static final String FIELD_USER_ID = "userId";
    private static final String FIELD_STATUS = "status";
    private static final String FIELD_CREATED_AT = "createdAt";

    private PaymentSpecification() {
    }

    public static Criteria hasId(UUID id) {
        if (id == null) {
            return null;
        }
        return Criteria.where(FIELD_ID).is(id);
    }

    public static Criteria hasOrderId(UUID orderId) {
        if (orderId == null) {
            return null;
        }
        return Criteria.where(FIELD_ORDER_ID).is(orderId);
    }

    public static Criteria hasUserId(Long userId) {
        if (userId == null) {
            return null;
        }
        return Criteria.where(FIELD_USER_ID).is(userId);
    }

    public static Criteria hasStatus(PaymentStatus status) {
        if (status == null) {
            return null;
        }
        return Criteria.where(FIELD_STATUS).is(status);
    }

    public static Criteria hasStatusesIn(List<PaymentStatus> statuses) {
        if (statuses == null || statuses.isEmpty()) {
            return null;
        }
        return Criteria.where(FIELD_STATUS).in(statuses);
    }

    public static Criteria statusNot(PaymentStatus excluded) {
        if (excluded == null) {
            return null;
        }
        return Criteria.where(FIELD_STATUS).ne(excluded);
    }

    public static Criteria createdAtBetween(Instant from, Instant to) {
        if (from == null && to == null) {
            return null;
        }
        if (from == null) {
            return Criteria.where(FIELD_CREATED_AT).lte(to);
        }
        if (to == null) {
            return Criteria.where(FIELD_CREATED_AT).gte(from);
        }
        return Criteria.where(FIELD_CREATED_AT).gte(from).lte(to);
    }

    public static Criteria allOf(Criteria... parts) {
        List<Criteria> list = Arrays.stream(parts).filter(Objects::nonNull).toList();
        if (list.isEmpty()) {
            return new Criteria();
        }
        if (list.size() == 1) {
            return list.get(0);
        }
        return new Criteria().andOperator(list.toArray(Criteria[]::new));
    }
}
