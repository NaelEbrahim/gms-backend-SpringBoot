package com.graduation.GMS.DTO.Response;

import com.graduation.GMS.Models.Class;
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
public class ClassResponse {
    private UserResponse coach;

    private Integer id;

    private String name;

    private String description;

    private String imagePath;

    private Float price;

    private Boolean isActive;

    private String myFeedBack;

    private LocalDateTime joinedAt;

    private List<ProgramResponse> programs;

    private List<UserResponse> subscribers;

    private List<UserFeedBackResponse> feedbacks;

    public ClassResponse(Integer id, String name, String description, String imagePath, Float price, UserResponse coach) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.imagePath = imagePath;
        this.price = price;
        this.coach = coach;
    }

    public static ClassResponse mapToClassResponse(Class classItem) {
        return new ClassResponse(
                classItem.getId(),
                classItem.getName(),
                classItem.getDescription(),
                classItem.getImagePath(),
                classItem.getPrice(),
                UserResponse.mapToUserResponse(classItem.getAuditCoach())
        );
    }

    public ClassResponse(UserResponse coach, Integer id, String name, String description, String imagePath, Float price, List<ProgramResponse> programs, List<UserResponse> subscribers, List<UserFeedBackResponse> feedbacks) {
        this.coach = coach;
        this.id = id;
        this.name = name;
        this.description = description;
        this.imagePath = imagePath;
        this.price = price;
        this.programs = programs;
        this.subscribers = subscribers;
        this.feedbacks = feedbacks;
    }
}

