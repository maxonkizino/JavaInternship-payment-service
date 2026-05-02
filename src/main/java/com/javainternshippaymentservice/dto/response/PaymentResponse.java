package com.javainternshippaymentservice.dto.response;

import com.javainternshippaymentservice.model.PaymentStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
public class PaymentResponse {

    private UUID id;
    private UUID orderId;
    private Long userId;
    private PaymentStatus status;
    private Instant timestamp;
    private BigDecimal paymentAmount;
    private Instant createdAt;
    private Instant updatedAt;
}
