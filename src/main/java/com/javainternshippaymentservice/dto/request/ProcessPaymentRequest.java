package com.javainternshippaymentservice.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Body for initiating payment processing (card validation + external random outcome).
 */
@Data
public class ProcessPaymentRequest {

    @NotNull
    private Long paymentCardId;
}
