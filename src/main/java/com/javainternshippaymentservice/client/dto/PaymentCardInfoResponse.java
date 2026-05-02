package com.javainternshippaymentservice.client.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class PaymentCardInfoResponse {

    private Long id;
    private String number;
    private String holder;
    private LocalDate expirationDate;
    private boolean active;
}
