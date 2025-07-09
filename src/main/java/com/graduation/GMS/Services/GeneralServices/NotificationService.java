package com.graduation.GMS.Services.GeneralServices;

import com.graduation.GMS.DTO.Response.NotificationResponse;
import com.graduation.GMS.Handlers.HandleCurrentUserSession;
import com.graduation.GMS.Models.Notification;
import com.graduation.GMS.Models.User;
import com.graduation.GMS.Models.User_Notification;
import com.graduation.GMS.Repositories.User_NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final SimpMessagingTemplate simpMessagingTemplate;


    private final User_NotificationRepository userNotificationRepository;

    public void sendNotification(User user, Notification notification) {
         log.info("Sending WS notification to user {} with payload {}", user.getId(), notification );

        simpMessagingTemplate.convertAndSendToUser(
                user.getId().toString(),
                "/notification",
                notification
        );

         User_Notification user_notification = new User_Notification();
         user_notification.setUser(user);
         user_notification.setNotification(notification);
         user_notification.setSendAt(LocalDateTime.now());
         userNotificationRepository.save(user_notification);

    }
    public void sendNotificationToUsers(List<User> users, Notification notification) {
        for (User user : users) {
            log.info("Sending WS notification to user {} with payload {}", user.getId(), notification);

            simpMessagingTemplate.convertAndSendToUser(
                    user.getId().toString(),
                    "/notification",
                    notification
            );

            User_Notification userNotification = new User_Notification();
            userNotification.setUser(user);
            userNotification.setNotification(notification);
            userNotification.setSendAt(LocalDateTime.now());

            userNotificationRepository.save(userNotification);
        }
    }

    public ResponseEntity<?> getMyNotifications() {
        List<User_Notification> userNotifications = userNotificationRepository.findByUser(HandleCurrentUserSession.getCurrentUser());

        if (userNotifications.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "No Notifications found"));
        }
        var response = userNotifications.stream()
                .map(un -> {
                    Notification n = un.getNotification();
                    return new NotificationResponse(
                            n.getId(),
                            n.getTitle(),
                            n.getContent(),
                            n.getCreatedAt(),
                            un.getSendAt()
                    );
                })
                .collect(Collectors.toList());

        return ResponseEntity.status(HttpStatus.OK)
                .body(response);

    }


}
