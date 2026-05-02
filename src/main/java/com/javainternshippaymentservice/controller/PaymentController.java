package com.javainternshippaymentservice.controller;

import com.javainternshippaymentservice.dto.request.ProcessPaymentRequest;
import com.javainternshippaymentservice.dto.request.create.CreatePaymentRequest;
import com.javainternshippaymentservice.dto.request.update.UpdatePaymentRequest;
import com.javainternshippaymentservice.dto.request.update.UpdatePaymentStatusRequest;
import com.javainternshippaymentservice.dto.response.PaymentResponse;
import com.javainternshippaymentservice.logging.ControllerLogger;
import com.javainternshippaymentservice.model.PaymentStatus;
import com.javainternshippaymentservice.service.PaymentService;
import com.javainternshippaymentservice.util.PaymentFilterParamUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Validated
public class PaymentController {

    private final PaymentService paymentService;
    private final ControllerLogger controllerLogger;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<Page<PaymentResponse>> getPayments(
            @RequestParam(required = false) PaymentStatus status,
            @RequestParam(required = false) List<PaymentStatus> statuses,
            @RequestParam(required = false) Instant createdAtFrom,
            @RequestParam(required = false) Instant createdAtTo,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) UUID orderId,
            Pageable pageable) {
        controllerLogger.methodCalled("PaymentController", "getPayments", status, statuses, createdAtFrom, createdAtTo, userId, orderId);
        if (PaymentFilterParamUtils.hasAnyFilter(status, statuses, createdAtFrom, createdAtTo, userId, orderId)) {
            return ResponseEntity.ok(paymentService.getPaymentsWithFilter(
                    status, statuses, createdAtFrom, createdAtTo, userId, orderId, pageable));
        }
        return ResponseEntity.ok(paymentService.getAllPayments(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<PaymentResponse> getPaymentById(@PathVariable UUID id) {
        controllerLogger.methodCalled("PaymentController", "getPaymentById", id);
        return ResponseEntity.ok(paymentService.getPaymentById(id));
    }

    @GetMapping("/order/{orderId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<PaymentResponse> getPaymentByOrderId(@PathVariable UUID orderId) {
        controllerLogger.methodCalled("PaymentController", "getPaymentByOrderId", orderId);
        return ResponseEntity.ok(paymentService.getPaymentByOrderId(orderId));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<PaymentResponse> createPayment(
            @Valid @RequestBody CreatePaymentRequest request,
            @RequestParam Long paymentCardId) {
        controllerLogger.methodCalled("PaymentController", "createPayment", paymentCardId, request.getOrderId());
        PaymentResponse created = paymentService.createPayment(request, paymentCardId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<PaymentResponse> updatePayment(
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePaymentRequest request,
            @RequestParam Long paymentCardId) {
        controllerLogger.methodCalled("PaymentController", "updatePayment", id, paymentCardId);
        return ResponseEntity.ok(paymentService.updatePayment(id, request, paymentCardId));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<PaymentResponse> updatePaymentStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePaymentStatusRequest request) {
        controllerLogger.methodCalled("PaymentController", "updatePaymentStatus", id, request.getStatus(), request.getPaymentCardId());
        return ResponseEntity.ok(paymentService.updatePaymentStatus(
                id, request.getStatus(), request.getPaymentCardId()));
    }

    @PostMapping("/{id}/processing")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<PaymentResponse> processPayment(
            @PathVariable UUID id,
            @Valid @RequestBody ProcessPaymentRequest request) {
        controllerLogger.methodCalled("PaymentController", "processPayment", id, request.getPaymentCardId());
        return ResponseEntity.ok(paymentService.processPaymentByExternalRandom(id, request.getPaymentCardId()));
    }
}
