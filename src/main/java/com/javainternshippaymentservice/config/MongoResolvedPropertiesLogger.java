package com.javainternshippaymentservice.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MongoResolvedPropertiesLogger implements CommandLineRunner {

    @Value("${spring.data.mongodb.host:NOT_SET}")
    private String host;

    @Value("${spring.data.mongodb.port:NOT_SET}")
    private String port;

    @Value("${spring.data.mongodb.uri:NOT_SET}")
    private String uri;

    @Value("${spring.data.mongodb.database:NOT_SET}")
    private String database;

    @Override
    public void run(String... args) {
        log.info("Resolved Mongo properties: host={}, port={}, uri={}, database={}", host, port, uri, database);
    }
}

