package com.graduation.GMS.DTO.Response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EventResponse {

    private Integer id;

    private UserResponse admin;

    private String title;

    private String description;

    private LocalDateTime startedAt;

    private List<PrizeResponse> prizes;

}
