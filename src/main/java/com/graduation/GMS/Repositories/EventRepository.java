package com.graduation.GMS.Repositories;

import com.graduation.GMS.Models.Event;
import com.graduation.GMS.Models.Session;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Integer> {
    Optional<Event> findByTitle(String title);

    @Query("SELECT e FROM Event e ORDER BY e.id")
    Page<Event> findAllPageable(Pageable pageable);

}
