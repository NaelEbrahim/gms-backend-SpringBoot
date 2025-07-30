package com.graduation.GMS.Models;

import com.graduation.GMS.Models.Enums.Day;
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
public class Program_Workout {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer reps;

    private Integer sets;

    @Enumerated(EnumType.STRING)
    private Day day;

    @Enumerated(EnumType.STRING)
    private Muscle muscle;

    @ManyToOne
    @JoinColumn(name = "program_id")
    private Program program;

    @ManyToOne
    @JoinColumn(name = "workout_id")
    private Workout workout;

    @OneToMany(mappedBy = "programWorkout", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserProgress> userProgresses;

}
