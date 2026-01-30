package com.graduation.GMS.DTO.Response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EventParticipantResponse {
    private String title;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startedAt;

    private List<ParticipantResponse> participants;
}
