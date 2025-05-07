package com.graduation.GMS.Models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    @ManyToOne
    @JoinColumn(name = "program_id")
    private Program program;

    @ManyToOne
    @JoinColumn(name = "workout_id")
    private Workout workout;

}
