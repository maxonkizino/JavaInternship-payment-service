package com.javainternshippaymentservice.util;

import com.javainternshippaymentservice.model.PaymentStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class PaymentFilterParamUtils {

    private PaymentFilterParamUtils() {
    }

    public static boolean hasAnyFilter(
            PaymentStatus status,
            List<PaymentStatus> statuses,
            Instant createdAtFrom,
            Instant createdAtTo,
            Long userId,
            UUID orderId) {
        return status != null
                || (statuses != null && !statuses.isEmpty())
                || createdAtFrom != null
                || createdAtTo != null
                || userId != null
                || orderId != null;
    }
}
