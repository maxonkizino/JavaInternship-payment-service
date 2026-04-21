package com.javainternshippaymentservice.dto.request.create;

import com.javainternshippaymentservice.model.PaymentStatus;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
public class CreatePaymentRequest {

    @NotNull
    private UUID orderId;

    @NotNull
    @Positive
    private Long userId;

    @NotNull
    private PaymentStatus status;

    @PastOrPresent
    private Instant timestamp;

    @NotNull
    @Positive
    @Digits(integer = 12, fraction = 2)
    private BigDecimal paymentAmount;
}
