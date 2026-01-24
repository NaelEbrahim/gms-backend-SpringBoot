package com.graduation.GMS.Controllers;

import com.graduation.GMS.Services.GeneralServices.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/me")
    public ResponseEntity<?> getMyNotifications() {
        return notificationService.getMyNotifications();
    }

    @DeleteMapping("/delete-notification/{notificationId}")
    public ResponseEntity<?> deleteNotification(@PathVariable Integer notificationId) {
        return notificationService.deleteNotification(notificationId);
    }

}
