package com.graduation.GMS.DTO.Response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Setter
@Getter
public class UserProgressResponse {

    private Integer id;

    private Float weight;

    private Integer duration;

    private String note;

    private LocalDate recordedAt;

}
