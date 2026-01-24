package com.graduation.GMS.Services.GeneralServices;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.graduation.GMS.DTO.Response.NotificationResponse;
import com.graduation.GMS.Handlers.HandleCurrentUserSession;
import com.graduation.GMS.Models.Notification;
import com.graduation.GMS.Models.User;
import com.graduation.GMS.Models.User_Notification;
import com.graduation.GMS.Repositories.User_NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.google.firebase.messaging.Message;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final User_NotificationRepository userNotificationRepository;

    @Transactional
    public void sendNotification(User user, Notification notification) {
        if (user.getFcmToken() != null) {
            log.info("Sending Firebase notification to user {} with payload {}", user.getId(), notification);

            sendFirebaseMessage(user.getFcmToken(), notification);

            User_Notification user_notification = new User_Notification();
            user_notification.setUser(user);
            user_notification.setNotification(notification);
            user_notification.setSendAt(LocalDateTime.now());
            userNotificationRepository.save(user_notification);
        }
    }

    @Transactional
    public void sendNotificationToUsers(List<User> users, Notification notification) {
        for (User user : users) {
            if (user.getFcmToken() != null) {
                log.info("Sending Firebase notification to user {} with payload {}", user.getId(), notification);

                sendFirebaseMessage(user.getFcmToken(), notification);

                User_Notification userNotification = new User_Notification();
                userNotification.setUser(user);
                userNotification.setNotification(notification);
                userNotification.setSendAt(LocalDateTime.now());
                userNotificationRepository.save(userNotification);
            }
        }
    }

    private void sendFirebaseMessage(String fcmToken, Notification notification) {
        Message message = Message.builder()
                .setToken(fcmToken)
                .setNotification(
                        com.google.firebase.messaging.Notification.builder()
                                .setTitle(notification.getTitle())
                                .setBody(notification.getContent())
                                .build()
                )
                .putData("id", notification.getId().toString())
                .putData("createdAt", notification.getCreatedAt().toString())
                .build();

        try {
            String response = FirebaseMessaging.getInstance().send(message);
            log.info("Firebase message sent successfully: {}", response);
        } catch (FirebaseMessagingException e) {
            log.error("Error sending Firebase message", e);
        }
    }

    public ResponseEntity<?> getMyNotifications() {
        List<User_Notification> userNotifications = userNotificationRepository.findByUser(HandleCurrentUserSession.getCurrentUser());

        var response = userNotifications.stream()
                .map(un -> {
                    Notification n = un.getNotification();
                    return new NotificationResponse(
                            un.getId(),
                            n.getTitle(),
                            n.getContent(),
                            n.getCreatedAt(),
                            un.getSendAt()
                    );
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    public ResponseEntity<?> deleteNotification(Integer id) {
        if (!userNotificationRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "notification id not found"));
        }
        userNotificationRepository.deleteById(id);
        return ResponseEntity.status(HttpStatus.OK)
                .body(Map.of("message", "notification deleted"));
    }

}
