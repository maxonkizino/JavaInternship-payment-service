package com.javainternshippaymentservice.client;

import com.javainternshippaymentservice.client.dto.PaymentCardInfoResponse;

/**
 * Outbound client to user-service for payment card data used during payment operations.
 */
public interface UserPaymentCardClient {

    /**
     * Loads a payment card by id from user-service ({@code GET /api/payment-cards/{id}}).
     *
     * @param paymentCardId card id
     * @return card snapshot
     */
    PaymentCardInfoResponse getPaymentCardById(Long paymentCardId);
}
