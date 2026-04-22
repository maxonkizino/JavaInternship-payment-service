package com.javainternshippaymentservice.controller;

import com.javainternshippaymentservice.client.CsrngRandomClient;
import com.javainternshippaymentservice.client.UserPaymentCardClient;
import com.javainternshippaymentservice.client.dto.PaymentCardInfoResponse;
import com.javainternshippaymentservice.model.Payment;
import com.javainternshippaymentservice.model.PaymentStatus;
import com.javainternshippaymentservice.repository.PaymentRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
@Import(PaymentControllerIntegrationTest.StubClientConfig.class)
class PaymentControllerIntegrationTest {

    @Container
    static final MongoDBContainer MONGO = new MongoDBContainer("mongo:7.0");

    @DynamicPropertySource
    static void mongoProps(DynamicPropertyRegistry registry) {
        registry.add(
                "spring.data.mongodb.uri",
                () -> MONGO.getReplicaSetUrl("javainternship-payment-service-test") + "&uuidRepresentation=standard"
        );
        registry.add("spring.data.mongodb.uuid-representation", () -> "standard");
    }

    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private StubUserPaymentCardClient stubUserPaymentCardClient;
    @Autowired
    private StubCsrngRandomClient stubCsrngRandomClient;

    @LocalServerPort
    private int port;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @AfterEach
    void cleanup() {
        paymentRepository.deleteAll();
        stubUserPaymentCardClient.setActive(true);
        stubUserPaymentCardClient.setReturnNull(false);
        stubCsrngRandomClient.setNextValue(8L);
    }

    @Test
    void createAndGetPayment_shouldWork() throws Exception {
        String body = """
                {
                  "orderId":"%s",
                  "userId":42,
                  "status":"CREATED",
                  "timestamp":"%s",
                  "paymentAmount":199.99
                }
                """.formatted(UUID.randomUUID(), Instant.now());

        HttpResponse<String> createResponse = send("POST", "/api/payments?paymentCardId=1", body);
        assertEquals(201, createResponse.statusCode());
        String id = extractField(createResponse.body(), "id");

        HttpResponse<String> getResponse = send("GET", "/api/payments/" + id, null);
        assertEquals(200, getResponse.statusCode());
        assertTrue(getResponse.body().contains("\"id\":\"" + id + "\""));
    }

    @Test
    void getPaymentsWithFilter_shouldReturnOnlyMatchingItems() throws Exception {
        paymentRepository.save(payment(10L, PaymentStatus.CREATED, UUID.randomUUID()));
        paymentRepository.save(payment(11L, PaymentStatus.PROCESSING, UUID.randomUUID()));
        paymentRepository.save(payment(10L, PaymentStatus.CANCELLED, UUID.randomUUID()));

        HttpResponse<String> response = send("GET", "/api/payments?userId=10&statuses=CREATED&page=0&size=10", null);
        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("\"userId\":10"));
        assertTrue(response.body().contains("\"status\":\"CREATED\""));
    }

    @Test
    void updateStatus_shouldPersistStatusChange() throws Exception {
        Payment payment = paymentRepository.save(payment(7L, PaymentStatus.CREATED, UUID.randomUUID()));
        String body = """
                {
                  "status":"SUCCEEDED",
                  "paymentCardId":1
                }
                """;

        HttpResponse<String> response = send("PATCH", "/api/payments/" + payment.getId() + "/status", body);
        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("\"status\":\"SUCCEEDED\""));
    }

    @Test
    void processPayment_shouldUseCsrngValue() throws Exception {
        Payment payment = paymentRepository.save(payment(7L, PaymentStatus.PROCESSING, UUID.randomUUID()));
        stubCsrngRandomClient.setNextValue(7L);
        String body = """
                {
                  "paymentCardId":1
                }
                """;

        HttpResponse<String> response = send("POST", "/api/payments/" + payment.getId() + "/processing", body);
        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("\"status\":\"FAILED\""));
    }

    @Test
    void createPayment_shouldReturnBadRequestForInactiveCard() throws Exception {
        stubUserPaymentCardClient.setActive(false);
        String body = """
                {
                  "orderId":"%s",
                  "userId":42,
                  "status":"CREATED",
                  "timestamp":"%s",
                  "paymentAmount":50
                }
                """.formatted(UUID.randomUUID(), Instant.now());

        HttpResponse<String> response = send("POST", "/api/payments?paymentCardId=1", body);
        assertEquals(400, response.statusCode());
        assertTrue(response.body().contains("Payment card is not active"));
    }

    @Test
    void getPaymentById_shouldReturn404ForMissingPayment() throws Exception {
        HttpResponse<String> response = send("GET", "/api/payments/" + UUID.randomUUID(), null);
        assertEquals(404, response.statusCode());
    }

    private HttpResponse<String> send(String method, String path, String jsonBody) throws Exception {
        HttpRequest.BodyPublisher publisher = jsonBody == null
                ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(jsonBody);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + path))
                .method(method, publisher)
                .header("Content-Type", "application/json")
                .build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private static String extractField(String json, String fieldName) {
        String key = "\"" + fieldName + "\":\"";
        int start = json.indexOf(key);
        if (start < 0) {
            return "";
        }
        int valueStart = start + key.length();
        int end = json.indexOf("\"", valueStart);
        if (end < 0) {
            return "";
        }
        return json.substring(valueStart, end);
    }

    private static Payment payment(Long userId, PaymentStatus status, UUID orderId) {
        Payment payment = new Payment();
        payment.setId(UUID.randomUUID());
        payment.setOrderId(orderId);
        payment.setUserId(userId);
        payment.setStatus(status);
        payment.setTimestamp(Instant.now());
        payment.setPaymentAmount(BigDecimal.valueOf(100));
        return payment;
    }

    @TestConfiguration
    static class StubClientConfig {

        @Bean
        @Primary
        StubUserPaymentCardClient stubUserPaymentCardClient() {
            return new StubUserPaymentCardClient();
        }

        @Bean
        @Primary
        StubCsrngRandomClient stubCsrngRandomClient() {
            return new StubCsrngRandomClient();
        }
    }

    static class StubUserPaymentCardClient implements UserPaymentCardClient {
        private boolean active = true;
        private boolean returnNull = false;

        @Override
        public PaymentCardInfoResponse getPaymentCardById(Long paymentCardId) {
            if (returnNull) {
                return null;
            }
            PaymentCardInfoResponse response = new PaymentCardInfoResponse();
            response.setId(paymentCardId);
            response.setActive(active);
            return response;
        }

        void setActive(boolean active) {
            this.active = active;
        }

        void setReturnNull(boolean returnNull) {
            this.returnNull = returnNull;
        }
    }

    static class StubCsrngRandomClient implements CsrngRandomClient {
        private long nextValue = 8L;

        @Override
        public long fetchRandomInclusive() {
            return nextValue;
        }

        void setNextValue(long nextValue) {
            this.nextValue = nextValue;
        }
    }
}
