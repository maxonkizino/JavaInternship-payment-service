package com.javainternshippaymentservice.client;

import com.javainternshippaymentservice.client.dto.PaymentCardInfoResponse;

public interface UserPaymentCardClient {

    PaymentCardInfoResponse getPaymentCardById(Long paymentCardId);
}
