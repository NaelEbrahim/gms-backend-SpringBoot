package com.graduation.GMS.DTO.Request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PrizeRequest {

    private Integer event_id;

    @Size(max = 500, message = "description name must not exceed 500 characters")
    private String description;

    private String precondition;

}
