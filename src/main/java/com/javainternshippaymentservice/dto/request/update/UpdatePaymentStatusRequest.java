package com.javainternshippaymentservice.dto.request.update;

import com.javainternshippaymentservice.model.PaymentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdatePaymentStatusRequest {

    @NotNull
    private PaymentStatus status;

    @NotNull
    private Long paymentCardId;
}
