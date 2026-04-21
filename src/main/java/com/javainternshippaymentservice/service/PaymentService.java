package com.javainternshippaymentservice.service;

import com.javainternshippaymentservice.dto.request.create.CreatePaymentRequest;
import com.javainternshippaymentservice.dto.request.update.UpdatePaymentRequest;
import com.javainternshippaymentservice.dto.response.PaymentResponse;

import java.util.List;
import java.util.UUID;

public interface PaymentService {

    /**
     * Returns payment by its identifier.
     *
     * @param id payment identifier
     * @return payment view model
     */
    PaymentResponse getPaymentById(UUID id);

    /**
     * Returns payment by related order identifier.
     *
     * @param orderId order identifier
     * @return payment view model
     */
    PaymentResponse getPaymentByOrderId(UUID orderId);

    /**
     * Returns all persisted payments.
     *
     * @return list of payment view models
     */
    List<PaymentResponse> getAllPayments();

    /**
     * Creates payment after validating payment card state in user-service.
     *
     * @param createPaymentRequest payload for payment creation
     * @param paymentCardId        payment card identifier from user-service
     * @return created payment view model
     */
    PaymentResponse createPayment(CreatePaymentRequest createPaymentRequest, Long paymentCardId);

    /**
     * Updates payment after validating payment card state in user-service.
     *
     * @param id                   payment identifier
     * @param updatePaymentRequest payload for payment update
     * @param paymentCardId        payment card identifier from user-service
     * @return updated payment view model
     */
    PaymentResponse updatePayment(UUID id, UpdatePaymentRequest updatePaymentRequest, Long paymentCardId);

    /**
     * Removes payment by its identifier.
     *
     * @param id payment identifier
     */
    void deletePayment(UUID id);
}
