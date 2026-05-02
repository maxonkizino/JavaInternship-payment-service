package com.javainternshippaymentservice.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class MongoClientOverrideConfig {

    @Bean
    public MongoClient mongoClient(@Value("${spring.data.mongodb.uri}") String uri) {
        String safeUri = uri.replaceAll("://[^@/]+@", "://****@");
        log.info("Creating MongoClient from uri={}", safeUri);
        return MongoClients.create(uri);
    }
}

