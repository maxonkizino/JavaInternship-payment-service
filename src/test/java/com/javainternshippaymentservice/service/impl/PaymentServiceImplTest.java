package com.javainternshippaymentservice.service.impl;

import com.javainternshippaymentservice.client.CsrngRandomClient;
import com.javainternshippaymentservice.client.UserPaymentCardClient;
import com.javainternshippaymentservice.client.dto.PaymentCardInfoResponse;
import com.javainternshippaymentservice.dto.request.create.CreatePaymentRequest;
import com.javainternshippaymentservice.dto.request.update.UpdatePaymentRequest;
import com.javainternshippaymentservice.dto.response.PaymentResponse;
import com.javainternshippaymentservice.event.PaymentEventPublisher;
import com.javainternshippaymentservice.exception.PaymentCardOwnershipException;
import com.javainternshippaymentservice.exception.PaymentNotFoundException;
import com.javainternshippaymentservice.mapper.PaymentMapper;
import com.javainternshippaymentservice.model.Payment;
import com.javainternshippaymentservice.model.PaymentStatus;
import com.javainternshippaymentservice.repository.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.Criteria;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private PaymentMapper paymentMapper;
    @Mock
    private UserPaymentCardClient userPaymentCardClient;
    @Mock
    private CsrngRandomClient csrngRandomClient;
    @Mock
    private PaymentEventPublisher paymentEventPublisher;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Test
    void getAllPayments_shouldReturnMappedPage() {
        Pageable pageable = PageRequest.of(0, 2);
        Payment payment = paymentWithStatus(PaymentStatus.CREATED);
        PaymentResponse response = new PaymentResponse();
        response.setId(payment.getId());
        Page<Payment> page = new PageImpl<>(List.of(payment), pageable, 5);

        when(paymentRepository.findPageByCriteria(any(Criteria.class), eq(pageable))).thenReturn(page);
        when(paymentMapper.toResponses(List.of(payment))).thenReturn(List.of(response));

        Page<PaymentResponse> result = paymentService.getAllPayments(pageable);

        assertEquals(1, result.getContent().size());
        assertEquals(5, result.getTotalElements());
        assertEquals(response.getId(), result.getContent().getFirst().getId());
    }

    @Test
    void getPaymentsWithFilter_shouldCallRepositoryWithCriteriaAndMapResponses() {
        Pageable pageable = PageRequest.of(1, 3);
        Payment payment = paymentWithStatus(PaymentStatus.PROCESSING);
        PaymentResponse response = new PaymentResponse();
        response.setId(payment.getId());
        Page<Payment> page = new PageImpl<>(List.of(payment), pageable, 1);
        UUID orderId = UUID.randomUUID();
        List<PaymentStatus> statuses = List.of(PaymentStatus.CREATED, PaymentStatus.PROCESSING);

        when(paymentRepository.findPageByCriteria(any(Criteria.class), eq(pageable))).thenReturn(page);
        when(paymentMapper.toResponses(List.of(payment))).thenReturn(List.of(response));

        Page<PaymentResponse> result = paymentService.getPaymentsWithFilter(
                PaymentStatus.CREATED,
                statuses,
                Instant.now().minusSeconds(3600),
                Instant.now(),
                11L,
                orderId,
                pageable);

        ArgumentCaptor<Criteria> criteriaCaptor = ArgumentCaptor.forClass(Criteria.class);
        verify(paymentRepository).findPageByCriteria(criteriaCaptor.capture(), eq(pageable));
        assertNotNull(criteriaCaptor.getValue());
        assertEquals(4, result.getTotalElements());
        assertEquals(response.getId(), result.getContent().getFirst().getId());
    }

    @Test
    void getPaymentById_shouldReturnMappedResponse() {
        UUID id = UUID.randomUUID();
        Payment payment = paymentWithStatus(PaymentStatus.CREATED);
        PaymentResponse response = new PaymentResponse();
        response.setId(id);

        when(paymentRepository.findActiveById(id, PaymentStatus.CANCELLED)).thenReturn(Optional.of(payment));
        when(paymentMapper.toResponse(payment)).thenReturn(response);

        PaymentResponse result = paymentService.getPaymentById(id);

        assertEquals(id, result.getId());
    }

    @Test
    void getPaymentById_shouldThrowWhenNotFound() {
        UUID id = UUID.randomUUID();
        when(paymentRepository.findActiveById(id, PaymentStatus.CANCELLED)).thenReturn(Optional.empty());

        PaymentNotFoundException exception = assertThrows(
                PaymentNotFoundException.class,
                () -> paymentService.getPaymentById(id));

        assertTrue(exception.getMessage().contains(id.toString()));
    }

    @Test
    void createPayment_shouldAssignIdWhenMissingAndSave() {
        CreatePaymentRequest request = new CreatePaymentRequest();
        Payment paymentWithoutId = paymentWithStatus(PaymentStatus.CREATED);
        paymentWithoutId.setId(null);
        Payment saved = paymentWithStatus(PaymentStatus.CREATED);
        PaymentResponse response = new PaymentResponse();
        response.setId(saved.getId());

        when(userPaymentCardClient.getPaymentCardById(10L)).thenReturn(activeCard(10L));
        when(paymentMapper.toEntity(request)).thenReturn(paymentWithoutId);
        when(paymentRepository.save(paymentWithoutId)).thenReturn(saved);
        when(paymentMapper.toResponse(saved)).thenReturn(response);

        PaymentResponse result = paymentService.createPayment(request, 10L);

        assertEquals(saved.getId(), result.getId());
        assertNotNull(paymentWithoutId.getId());
        verify(paymentEventPublisher).publishCreatePayment(saved);
    }

    @Test
    void createPayment_shouldThrowWhenCardMissing() {
        CreatePaymentRequest request = new CreatePaymentRequest();
        when(userPaymentCardClient.getPaymentCardById(99L)).thenReturn(null);

        PaymentCardOwnershipException exception = assertThrows(
                PaymentCardOwnershipException.class,
                () -> paymentService.createPayment(request, 99L));

        assertTrue(exception.getMessage().contains("Payment card not found"));
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void updatePayment_shouldThrowWhenCardInactive() {
        UUID id = UUID.randomUUID();
        UpdatePaymentRequest request = new UpdatePaymentRequest();
        Payment existing = paymentWithStatus(PaymentStatus.CREATED);

        when(paymentRepository.findActiveById(id, PaymentStatus.CANCELLED)).thenReturn(Optional.of(existing));
        when(userPaymentCardClient.getPaymentCardById(7L)).thenReturn(inactiveCard(7L));

        PaymentCardOwnershipException exception = assertThrows(
                PaymentCardOwnershipException.class,
                () -> paymentService.updatePayment(id, request, 7L));

        assertTrue(exception.getMessage().contains("not active"));
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void updatePayment_shouldMapAndSave() {
        UUID id = UUID.randomUUID();
        UpdatePaymentRequest request = new UpdatePaymentRequest();
        Payment existing = paymentWithStatus(PaymentStatus.PROCESSING);
        Payment saved = paymentWithStatus(PaymentStatus.PROCESSING);
        PaymentResponse response = new PaymentResponse();
        response.setId(saved.getId());

        when(paymentRepository.findActiveById(id, PaymentStatus.CANCELLED)).thenReturn(Optional.of(existing));
        when(userPaymentCardClient.getPaymentCardById(7L)).thenReturn(activeCard(7L));
        when(paymentRepository.save(existing)).thenReturn(saved);
        when(paymentMapper.toResponse(saved)).thenReturn(response);

        PaymentResponse result = paymentService.updatePayment(id, request, 7L);

        verify(paymentMapper).toEntity(request, existing);
        assertEquals(saved.getId(), result.getId());
    }

    @Test
    void updatePaymentStatus_shouldThrowWhenStatusNull() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> paymentService.updatePaymentStatus(UUID.randomUUID(), null, 5L));
        assertTrue(exception.getMessage().contains("must not be null"));
    }

    @Test
    void updatePaymentStatus_shouldSetAndSaveNewStatus() {
        UUID id = UUID.randomUUID();
        Payment existing = paymentWithStatus(PaymentStatus.CREATED);
        PaymentResponse response = new PaymentResponse();
        response.setStatus(PaymentStatus.SUCCEEDED);

        when(paymentRepository.findActiveById(id, PaymentStatus.CANCELLED)).thenReturn(Optional.of(existing));
        when(userPaymentCardClient.getPaymentCardById(5L)).thenReturn(activeCard(5L));
        when(paymentRepository.save(existing)).thenReturn(existing);
        when(paymentMapper.toResponse(existing)).thenReturn(response);

        PaymentResponse result = paymentService.updatePaymentStatus(id, PaymentStatus.SUCCEEDED, 5L);

        assertEquals(PaymentStatus.SUCCEEDED, existing.getStatus());
        assertEquals(PaymentStatus.SUCCEEDED, result.getStatus());
    }

    @Test
    void processPaymentByExternalRandom_shouldThrowForInvalidCurrentStatus() {
        UUID id = UUID.randomUUID();
        Payment existing = paymentWithStatus(PaymentStatus.CANCELLED);
        when(paymentRepository.findActiveById(id, PaymentStatus.CANCELLED)).thenReturn(Optional.of(existing));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> paymentService.processPaymentByExternalRandom(id, 1L));

        assertTrue(exception.getMessage().contains("CREATED or PROCESSING"));
        verify(csrngRandomClient, never()).fetchRandomInclusive();
    }

    @Test
    void processPaymentByExternalRandom_shouldSetSucceededForEvenRandom() {
        UUID id = UUID.randomUUID();
        Payment existing = paymentWithStatus(PaymentStatus.CREATED);
        PaymentResponse response = new PaymentResponse();
        response.setStatus(PaymentStatus.SUCCEEDED);

        when(paymentRepository.findActiveById(id, PaymentStatus.CANCELLED)).thenReturn(Optional.of(existing));
        when(userPaymentCardClient.getPaymentCardById(3L)).thenReturn(activeCard(3L));
        when(csrngRandomClient.fetchRandomInclusive()).thenReturn(8L);
        when(paymentRepository.save(existing)).thenReturn(existing);
        when(paymentMapper.toResponse(existing)).thenReturn(response);

        PaymentResponse result = paymentService.processPaymentByExternalRandom(id, 3L);

        assertEquals(PaymentStatus.SUCCEEDED, existing.getStatus());
        assertNotNull(existing.getTimestamp());
        assertEquals(PaymentStatus.SUCCEEDED, result.getStatus());
    }

    @Test
    void processPaymentByExternalRandom_shouldSetFailedForOddRandom() {
        UUID id = UUID.randomUUID();
        Payment existing = paymentWithStatus(PaymentStatus.PROCESSING);
        PaymentResponse response = new PaymentResponse();
        response.setStatus(PaymentStatus.FAILED);

        when(paymentRepository.findActiveById(id, PaymentStatus.CANCELLED)).thenReturn(Optional.of(existing));
        when(userPaymentCardClient.getPaymentCardById(4L)).thenReturn(activeCard(4L));
        when(csrngRandomClient.fetchRandomInclusive()).thenReturn(7L);
        when(paymentRepository.save(existing)).thenReturn(existing);
        when(paymentMapper.toResponse(existing)).thenReturn(response);

        PaymentResponse result = paymentService.processPaymentByExternalRandom(id, 4L);

        assertEquals(PaymentStatus.FAILED, existing.getStatus());
        assertNotNull(existing.getTimestamp());
        assertEquals(PaymentStatus.FAILED, result.getStatus());
    }

    @Test
    void getPaymentByOrderId_shouldThrowWhenNotFound() {
        UUID orderId = UUID.randomUUID();
        when(paymentRepository.findActiveByOrderId(orderId, PaymentStatus.CANCELLED)).thenReturn(Optional.empty());

        PaymentNotFoundException exception = assertThrows(
                PaymentNotFoundException.class,
                () -> paymentService.getPaymentByOrderId(orderId));

        assertTrue(exception.getMessage().contains(orderId.toString()));
    }

    private static Payment paymentWithStatus(PaymentStatus status) {
        Payment payment = new Payment();
        payment.setId(UUID.randomUUID());
        payment.setOrderId(UUID.randomUUID());
        payment.setUserId(1L);
        payment.setStatus(status);
        payment.setTimestamp(Instant.now());
        payment.setPaymentAmount(BigDecimal.TEN);
        return payment;
    }

    private static PaymentCardInfoResponse activeCard(Long id) {
        PaymentCardInfoResponse card = new PaymentCardInfoResponse();
        card.setId(id);
        card.setActive(true);
        return card;
    }

    private static PaymentCardInfoResponse inactiveCard(Long id) {
        PaymentCardInfoResponse card = new PaymentCardInfoResponse();
        card.setId(id);
        card.setActive(false);
        return card;
    }
}
