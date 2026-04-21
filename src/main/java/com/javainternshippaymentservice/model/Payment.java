package com.javainternshippaymentservice.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import lombok.Data;

@Document(collection = "payments")
@Data
public class Payment extends BaseAuditingEntity {

    @Id
    private String id;

    private UUID orderId;

    private Long userId;

    private PaymentStatus status;

    private Instant processedAt;

    private BigDecimal paymentAmount;

}
