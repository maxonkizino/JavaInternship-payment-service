package com.javainternshippaymentservice.event;

import com.javainternshippaymentservice.model.Payment;
import com.javainternshippaymentservice.model.PaymentStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class KafkaPaymentEventPublisherTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private KafkaPaymentEventPublisher publisher;

    @Test
    void publishCreatePayment_shouldSendEventWhenKafkaEnabled() {
        ReflectionTestUtils.setField(publisher, "kafkaEnabled", true);
        ReflectionTestUtils.setField(publisher, "createPaymentTopic", "create-payment");

        Payment payment = new Payment();
        payment.setId(UUID.randomUUID());
        payment.setOrderId(UUID.randomUUID());
        payment.setStatus(PaymentStatus.CREATED);

        publisher.publishCreatePayment(payment);

        ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);
        verify(kafkaTemplate).send(
                org.mockito.ArgumentMatchers.eq("create-payment"),
                org.mockito.ArgumentMatchers.eq(payment.getOrderId().toString()),
                payloadCaptor.capture()
        );

        CreatePaymentEvent event = (CreatePaymentEvent) payloadCaptor.getValue();
        assertEquals(payment.getId(), event.getPaymentId());
        assertEquals(payment.getOrderId(), event.getOrderId());
        assertEquals("CREATED", event.getStatus());
        assertNotNull(event.getEventTime());
    }

    @Test
    void publishCreatePayment_shouldDoNothingWhenKafkaDisabled() {
        ReflectionTestUtils.setField(publisher, "kafkaEnabled", false);
        ReflectionTestUtils.setField(publisher, "createPaymentTopic", "create-payment");

        Payment payment = new Payment();
        payment.setId(UUID.randomUUID());
        payment.setOrderId(UUID.randomUUID());
        payment.setStatus(PaymentStatus.CREATED);

        publisher.publishCreatePayment(payment);

        verify(kafkaTemplate, never()).send(
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.any()
        );
    }
}
