package com.graduation.GMS.DTO.Response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PrizeResponse {
    private Integer id;

    private String description;

    private String precondition;
}
