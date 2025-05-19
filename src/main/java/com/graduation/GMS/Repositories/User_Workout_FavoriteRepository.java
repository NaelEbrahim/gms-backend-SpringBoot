package com.graduation.GMS.Repositories;

import com.graduation.GMS.Models.User;
import com.graduation.GMS.Models.User_Workout_favorite;
import com.graduation.GMS.Models.Workout;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface User_Workout_FavoriteRepository extends JpaRepository<User_Workout_favorite, Integer> {
    Optional<User_Workout_favorite> findByUserAndWorkout(User user, Workout workout);

    List<User_Workout_favorite> findAllByUser(User user);

}
