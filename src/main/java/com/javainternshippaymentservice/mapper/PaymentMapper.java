package com.javainternshippaymentservice.mapper;

import com.javainternshippaymentservice.dto.request.create.CreatePaymentRequest;
import com.javainternshippaymentservice.dto.request.update.UpdatePaymentRequest;
import com.javainternshippaymentservice.dto.response.PaymentResponse;
import com.javainternshippaymentservice.model.Payment;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PaymentMapper {

    PaymentResponse toResponse(Payment payment);

    Payment toEntity(CreatePaymentRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void toEntity(UpdatePaymentRequest request, @MappingTarget Payment payment);

    List<PaymentResponse> toResponses(List<Payment> payments);

}
