package com.graduation.GMS.Models;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DietPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 50, nullable = false)
    private String title;

//    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
//    private LocalDateTime startedAt;

    @Column(updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @Column(insertable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastModifiedAt;

    @ManyToOne
    @JoinColumn(name = "coach_id")
    private User coach;

    @OneToMany(mappedBy = "diet_plan", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<User_Diet> userDietList;

    @OneToMany(mappedBy = "diet_plan", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Plan_Meal> planMealList;

}
