package com.graduation.GMS.DTO.Response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClassResponse {
    private UserResponse coach;

    private Integer id;

    private String name;

    private String description;

    private String imagePath;

    private Float price;

    private List<ProgramResponse> programs;

    private List<UserResponse> subscribers;

    private List<UserFeedBackResponse> feedbacks;
}

