package com.javainternshippaymentservice.client.impl;

import com.javainternshippaymentservice.client.UserPaymentCardClient;
import com.javainternshippaymentservice.client.dto.PaymentCardInfoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class UserPaymentCardClientImpl implements UserPaymentCardClient {

    private final RestClient userServiceRestClient;

    @Override
    public PaymentCardInfoResponse getPaymentCardById(Long paymentCardId) {
        return userServiceRestClient.get()
                .uri("/api/payment-cards/{id}", paymentCardId)
                .retrieve()
                .body(PaymentCardInfoResponse.class);
    }
}
