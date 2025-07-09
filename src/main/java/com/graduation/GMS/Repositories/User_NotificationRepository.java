package com.graduation.GMS.Repositories;

import com.graduation.GMS.Models.User;
import com.graduation.GMS.Models.User_Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface User_NotificationRepository extends JpaRepository<User_Notification,Integer> {

    List<User_Notification> findByUser(User currentUser);
}
