package com.javainternshippaymentservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * HTTP client for CSRNG Lite ({@code https://csrng.net/csrng/csrng.php}).
 */
@Configuration
public class CsrngClientConfig {

    @Bean
    public RestClient csrngRestClient(@Value("${app.csrng.base-url}") String baseUrl) {
        return RestClient.builder().baseUrl(baseUrl).build();
    }
}
