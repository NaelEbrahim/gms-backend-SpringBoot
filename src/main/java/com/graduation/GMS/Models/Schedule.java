package com.graduation.GMS.Models;

import com.graduation.GMS.Models.Enums.Day;
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
public class Schedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Day dayNum;

    @ManyToOne
    @JoinColumn(name = "user_program_id")
    private User_Program user_program;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

}
