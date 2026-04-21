package com.javainternshippaymentservice.service.impl;

import com.javainternshippaymentservice.client.UserPaymentCardClient;
import com.javainternshippaymentservice.client.dto.PaymentCardInfoResponse;
import com.javainternshippaymentservice.dto.request.create.CreatePaymentRequest;
import com.javainternshippaymentservice.dto.request.update.UpdatePaymentRequest;
import com.javainternshippaymentservice.dto.response.PaymentResponse;
import com.javainternshippaymentservice.exception.PaymentCardOwnershipException;
import com.javainternshippaymentservice.exception.PaymentNotFoundException;
import com.javainternshippaymentservice.mapper.PaymentMapper;
import com.javainternshippaymentservice.model.Payment;
import com.javainternshippaymentservice.model.PaymentStatus;
import com.javainternshippaymentservice.repository.PaymentRepository;
import com.javainternshippaymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private static final String PAYMENT_NOT_FOUND_MESSAGE = "Payment not found with id: ";
    private static final String PAYMENT_BY_ORDER_NOT_FOUND_MESSAGE = "Payment not found with orderId: ";
    private static final String CARD_NOT_ACTIVE_MESSAGE = "Payment card is not active: ";
    private static final String CARD_NOT_FOUND_MESSAGE = "Payment card not found: ";

    private static final PaymentStatus EXCLUDED_LIST_STATUS = PaymentStatus.CANCELLED;

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final UserPaymentCardClient userPaymentCardClient;

    @Override
    public PaymentResponse getPaymentById(UUID id) {
        Payment payment = paymentRepository.findActiveById(id, EXCLUDED_LIST_STATUS)
                .orElseThrow(() -> new PaymentNotFoundException(PAYMENT_NOT_FOUND_MESSAGE + id));
        return paymentMapper.toResponse(payment);
    }

    @Override
    public PaymentResponse getPaymentByOrderId(UUID orderId) {
        Payment payment = paymentRepository.findActiveByOrderId(orderId, EXCLUDED_LIST_STATUS)
                .orElseThrow(() -> new PaymentNotFoundException(PAYMENT_BY_ORDER_NOT_FOUND_MESSAGE + orderId));
        return paymentMapper.toResponse(payment);
    }

    @Override
    public List<PaymentResponse> getAllPayments() {
        return paymentMapper.toResponses(paymentRepository.findAllActive(EXCLUDED_LIST_STATUS));
    }

    @Override
    public PaymentResponse createPayment(CreatePaymentRequest createPaymentRequest, Long paymentCardId) {
        validatePaymentCardForPayment(paymentCardId);
        Payment payment = paymentMapper.toEntity(createPaymentRequest);
        Payment savedPayment = paymentRepository.save(payment);
        return paymentMapper.toResponse(savedPayment);
    }

    @Override
    public PaymentResponse updatePayment(UUID id, UpdatePaymentRequest updatePaymentRequest, Long paymentCardId) {
        Payment payment = paymentRepository.findActiveById(id, EXCLUDED_LIST_STATUS)
                .orElseThrow(() -> new PaymentNotFoundException(PAYMENT_NOT_FOUND_MESSAGE + id));

        validatePaymentCardForPayment(paymentCardId);
        paymentMapper.toEntity(updatePaymentRequest, payment);
        Payment updatedPayment = paymentRepository.save(payment);
        return paymentMapper.toResponse(updatedPayment);
    }

    @Override
    public PaymentResponse updatePaymentStatus(UUID id, PaymentStatus newStatus, Long paymentCardId) {
        if (newStatus == null) {
            throw new IllegalArgumentException("Status must not be null");
        }
        Payment payment = paymentRepository.findActiveById(id, EXCLUDED_LIST_STATUS)
                .orElseThrow(() -> new PaymentNotFoundException(PAYMENT_NOT_FOUND_MESSAGE + id));
        validatePaymentCardForPayment(paymentCardId);
        payment.setStatus(newStatus);
        return paymentMapper.toResponse(paymentRepository.save(payment));
    }

    private void validatePaymentCardForPayment(Long paymentCardId) {
        PaymentCardInfoResponse paymentCard = userPaymentCardClient.getPaymentCardById(paymentCardId);
        if (paymentCard == null || paymentCard.getId() == null) {
            throw new PaymentCardOwnershipException(CARD_NOT_FOUND_MESSAGE + paymentCardId);
        }
        if (!paymentCard.isActive()) {
            throw new PaymentCardOwnershipException(CARD_NOT_ACTIVE_MESSAGE + paymentCardId);
        }
    }
}
