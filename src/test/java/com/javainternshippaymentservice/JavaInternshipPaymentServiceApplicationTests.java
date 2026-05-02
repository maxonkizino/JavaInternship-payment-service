package com.javainternshippaymentservice;

import com.javainternshippaymentservice.config.TestMongoClientConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
@Import(TestMongoClientConfig.class)
class JavaInternshipPaymentServiceApplicationTests {

    @Container
    static final MongoDBContainer MONGO = new MongoDBContainer("mongo:7.0");

    @DynamicPropertySource
    static void mongoProps(DynamicPropertyRegistry registry) {
        String baseUri = MONGO.getReplicaSetUrl("javainternship-payment-service-test");
        String mongoUri = baseUri + (baseUri.contains("?") ? "&" : "?") + "uuidRepresentation=standard";
        registry.add(
                "spring.data.mongodb.uri",
                () -> mongoUri
        );
        registry.add("spring.data.mongodb.uuid-representation", () -> "standard");
        registry.add("spring.data.mongodb.uuidRepresentation", () -> "standard");
    }

    @Test
    void contextLoads() {
    }

}
