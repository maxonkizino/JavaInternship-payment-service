package com.javainternshippaymentservice.event;

import com.javainternshippaymentservice.model.Payment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaPaymentEventPublisher implements PaymentEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.kafka.topics.create-payment}")
    private String createPaymentTopic;

    @Value("${app.kafka.enabled:true}")
    private boolean kafkaEnabled;

    @Override
    public void publishCreatePayment(Payment payment) {
        if (!kafkaEnabled) {
            return;
        }

        CreatePaymentEvent event = new CreatePaymentEvent(
                payment.getId(),
                payment.getOrderId(),
                payment.getStatus() == null ? null : payment.getStatus().name(),
                Instant.now()
        );

        try {
            kafkaTemplate.send(createPaymentTopic, payment.getOrderId().toString(), event);
        } catch (Exception ex) {
            log.error("Failed to publish create-payment event for paymentId={}", payment.getId(), ex);
        }
    }
}
