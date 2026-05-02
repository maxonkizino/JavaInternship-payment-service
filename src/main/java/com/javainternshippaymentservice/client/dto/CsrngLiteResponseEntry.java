package com.javainternshippaymentservice.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;


@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CsrngLiteResponseEntry {

    private String status;
    private Long min;
    private Long max;
    private Long random;
    private String code;
    private String reason;
}
