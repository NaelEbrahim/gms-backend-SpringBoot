package com.graduation.GMS.Repositories;

import com.graduation.GMS.Models.Event;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Integer> {
    Optional<Event> findByTitle(String title);
}
