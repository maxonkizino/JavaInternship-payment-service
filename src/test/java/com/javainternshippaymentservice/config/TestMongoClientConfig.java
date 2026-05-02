package com.javainternshippaymentservice.config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.bson.UuidRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration(proxyBeanMethods = false)
public class TestMongoClientConfig {

    @Bean
    MongoClient mongoClient(@Value("${spring.data.mongodb.uri}") String mongoUri) {
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(mongoUri))
                .uuidRepresentation(UuidRepresentation.STANDARD)
                .build();
        return MongoClients.create(settings);
    }
}
