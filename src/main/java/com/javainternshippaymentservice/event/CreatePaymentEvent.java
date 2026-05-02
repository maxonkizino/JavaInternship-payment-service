package com.javainternshippaymentservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePaymentEvent {

    private UUID paymentId;
    private UUID orderId;
    private String status;
    private Instant eventTime;
}
