package com.javainternshippaymentservice.client.impl;

import com.javainternshippaymentservice.client.dto.PaymentCardInfoResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserPaymentCardClientImplTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private RestClient restClient;

    @Test
    void getPaymentCardById_shouldReturnDtoFromUserService() {
        PaymentCardInfoResponse expected = new PaymentCardInfoResponse();
        expected.setId(99L);
        expected.setActive(true);

        when(restClient.get().uri("/api/payment-cards/{id}", 99L).retrieve().body(PaymentCardInfoResponse.class))
                .thenReturn(expected);

        UserPaymentCardClientImpl client = new UserPaymentCardClientImpl(restClient);
        PaymentCardInfoResponse actual = client.getPaymentCardById(99L);

        assertEquals(99L, actual.getId());
        assertEquals(true, actual.isActive());
    }
}
