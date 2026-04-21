package com.javainternshippaymentservice.service.impl;

import com.javainternshippaymentservice.client.CsrngRandomClient;
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
import com.javainternshippaymentservice.model.specification.PaymentSpecification;
import com.javainternshippaymentservice.repository.PaymentRepository;
import com.javainternshippaymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.time.Instant;
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
    private final CsrngRandomClient csrngRandomClient;


    @Override
    public Page<PaymentResponse> getPaymentsWithFilter(
            PaymentStatus status,
            List<PaymentStatus> statuses,
            Instant createdAtFrom,
            Instant createdAtTo,
            Long userId,
            UUID orderId,
            Pageable pageable) {
        Criteria criteria = PaymentSpecification.allOf(
                PaymentSpecification.statusNot(EXCLUDED_LIST_STATUS),
                PaymentSpecification.hasStatus(status),
                PaymentSpecification.hasStatusesIn(statuses),
                PaymentSpecification.createdAtBetween(createdAtFrom, createdAtTo),
                PaymentSpecification.hasUserId(userId),
                PaymentSpecification.hasOrderId(orderId));
        return toResponsePage(paymentRepository.findPageByCriteria(criteria, pageable));
    }

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
    public Page<PaymentResponse> getAllPayments(Pageable pageable) {
        Criteria criteria = PaymentSpecification.allOf(PaymentSpecification.statusNot(EXCLUDED_LIST_STATUS));
        return toResponsePage(paymentRepository.findPageByCriteria(criteria, pageable));
    }



    @Override
    public PaymentResponse createPayment(CreatePaymentRequest createPaymentRequest, Long paymentCardId) {
        validatePaymentCardForPayment(paymentCardId);
        Payment payment = paymentMapper.toEntity(createPaymentRequest);
        if (payment.getId() == null) {
            payment.setId(UUID.randomUUID());
        }
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

    @Override
    public PaymentResponse processPaymentByExternalRandom(UUID id, Long paymentCardId) {
        Payment payment = paymentRepository.findActiveById(id, EXCLUDED_LIST_STATUS)
                .orElseThrow(() -> new PaymentNotFoundException(PAYMENT_NOT_FOUND_MESSAGE + id));
        PaymentStatus current = payment.getStatus();
        if (current != PaymentStatus.CREATED && current != PaymentStatus.PROCESSING) {
            throw new IllegalArgumentException(
                    "Payment can only be processed when status is CREATED or PROCESSING");
        }
        validatePaymentCardForPayment(paymentCardId);
        long random = csrngRandomClient.fetchRandomInclusive();
        PaymentStatus outcome = (random % 2L == 0L) ? PaymentStatus.SUCCEEDED : PaymentStatus.FAILED;
        payment.setStatus(outcome);
        payment.setTimestamp(Instant.now());
        return paymentMapper.toResponse(paymentRepository.save(payment));
    }

    private Page<PaymentResponse> toResponsePage(Page<Payment> page) {
        return new PageImpl<>(
                paymentMapper.toResponses(page.getContent()),
                page.getPageable(),
                page.getTotalElements());
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
