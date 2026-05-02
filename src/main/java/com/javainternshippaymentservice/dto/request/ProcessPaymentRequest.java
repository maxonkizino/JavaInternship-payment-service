package com.javainternshippaymentservice.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProcessPaymentRequest {

    @NotNull
    private Long paymentCardId;
}
