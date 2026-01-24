package com.graduation.GMS.DTO.Response;

import com.graduation.GMS.Models.SubscriptionHistory;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class SubscriptionHistoryResponse {

    private Integer id;

    private LocalDateTime paymentDate;

    private Float paymentAmount;

    private Float discountPercentage;

    private UserResponse user;

    private ClassResponse aClass;

    public SubscriptionHistoryResponse(SubscriptionHistory data) {
        this.id = data.getId();
        this.paymentDate = data.getPaymentDate();
        this.paymentAmount = data.getPaymentAmount();
        this.discountPercentage = data.getDiscountPercentage();
        this.user = UserResponse.mapToUserResponse(data.getUser());
        this.aClass = ClassResponse.mapToClassResponse(data.getAClass());
    }

}
