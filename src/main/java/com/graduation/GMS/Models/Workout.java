package com.graduation.GMS.Models;

import com.graduation.GMS.Models.Enums.Muscle;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Workout {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true)
    private String title;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Muscle primary_muscle;

    @Enumerated(EnumType.STRING)
    private Muscle secondary_muscles;

    private Float avg_calories;

    private String description;

    @OneToMany(mappedBy = "workout", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Program_Workout> programWorkoutList;

    @OneToMany(mappedBy = "workout", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<User_Workout_favorite> userWorkoutFavoriteList;

    @OneToMany(mappedBy = "workout", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<User_Workout_Progress> userWorkoutProgressList;

}
