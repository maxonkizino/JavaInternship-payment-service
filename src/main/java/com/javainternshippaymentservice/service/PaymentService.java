package com.javainternshippaymentservice.service;

import com.javainternshippaymentservice.dto.request.create.CreatePaymentRequest;
import com.javainternshippaymentservice.dto.request.update.UpdatePaymentRequest;
import com.javainternshippaymentservice.dto.response.PaymentResponse;
import com.javainternshippaymentservice.model.PaymentStatus;

import java.util.List;
import java.util.UUID;

/**
 * Application service for payments: persistence and validation against user-service payment cards.
 */
public interface PaymentService {

    /**
     * Returns a payment by identifier. Payments in {@link PaymentStatus#CANCELLED} are treated as removed and are not returned.
     *
     * @param id payment id
     * @return payment data
     */
    PaymentResponse getPaymentById(UUID id);

    /**
     * Returns a payment for the given order. Excludes {@link PaymentStatus#CANCELLED}.
     *
     * @param orderId order id
     * @return payment data
     */
    PaymentResponse getPaymentByOrderId(UUID orderId);

    /**
     * Returns all payments except those in {@link PaymentStatus#CANCELLED}.
     *
     * @return list of payments
     */
    List<PaymentResponse> getAllPayments();

    /**
     * Creates a payment after validating the payment card in user-service.
     *
     * @param createPaymentRequest create payload
     * @param paymentCardId      card id in user-service
     * @return created payment
     */
    PaymentResponse createPayment(CreatePaymentRequest createPaymentRequest, Long paymentCardId);

    /**
     * Updates mutable fields after validating the payment card in user-service. Excludes cancelled payments.
     *
     * @param id                   payment id
     * @param updatePaymentRequest update payload
     * @param paymentCardId      card id in user-service
     * @return updated payment
     */
    PaymentResponse updatePayment(UUID id, UpdatePaymentRequest updatePaymentRequest, Long paymentCardId);

    /**
     * Sets the payment status after validating the payment card in user-service. Use {@link PaymentStatus#CANCELLED}
     * instead of physical deletion. Excludes payments that are already cancelled.
     *
     * @param id             payment id
     * @param newStatus      target status (must not be {@code null})
     * @param paymentCardId card id in user-service
     * @return payment after status change
     */
    PaymentResponse updatePaymentStatus(UUID id, PaymentStatus newStatus, Long paymentCardId);
}
