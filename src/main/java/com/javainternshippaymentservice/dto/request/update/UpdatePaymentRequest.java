package com.javainternshippaymentservice.dto.request.update;

import com.javainternshippaymentservice.model.PaymentStatus;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
public class UpdatePaymentRequest {

    private UUID orderId;

    @Positive
    private Long userId;

    private PaymentStatus status;

    @PastOrPresent
    private Instant timestamp;

    @Positive
    @Digits(integer = 12, fraction = 2)
    private BigDecimal paymentAmount;
}
