package com.graduation.GMS.Repositories;

import com.graduation.GMS.Models.Event;
import com.graduation.GMS.Models.Event_Participant;
import com.graduation.GMS.Models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.beans.JavaBean;
import java.util.List;
import java.util.Optional;

public interface Event_ParticipantRepository extends JpaRepository<Event_Participant, Integer> {
    boolean existsByUserAndEvent(User user, Event event);
    List<Event_Participant> findByEvent(Event event);
    Optional<Event_Participant> findByUserAndEvent(User user, Event event);
}
