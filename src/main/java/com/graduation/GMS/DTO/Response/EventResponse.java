package com.graduation.GMS.DTO.Response;

import com.fasterxml.jackson.annotation.JsonFormat;
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

    private String imagePath;

    private LocalDateTime startedAt;

    private LocalDateTime endedAt;

    private List<PrizeResponse> prizes;

}
