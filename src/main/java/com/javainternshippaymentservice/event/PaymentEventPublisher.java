package com.javainternshippaymentservice.event;

import com.javainternshippaymentservice.model.Payment;

public interface PaymentEventPublisher {

    void publishCreatePayment(Payment payment);
}
