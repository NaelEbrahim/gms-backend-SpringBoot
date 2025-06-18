package com.graduation.GMS.Repositories;

import com.graduation.GMS.Models.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface MessageRepository extends JpaRepository<Message, Integer> {
}
