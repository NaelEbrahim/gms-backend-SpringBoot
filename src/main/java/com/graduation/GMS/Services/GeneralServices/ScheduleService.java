package com.graduation.GMS.Services.GeneralServices;


import com.graduation.GMS.Models.Notification;
import com.graduation.GMS.Models.Subscription;
import com.graduation.GMS.Models.SubscriptionHistory;
import com.graduation.GMS.Models.User;
import com.graduation.GMS.Repositories.NotificationRepository;
import com.graduation.GMS.Repositories.SubscriptionHistoryRepository;
import com.graduation.GMS.Repositories.SubscriptionRepository;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;


@Service
@AllArgsConstructor
public class ScheduleService {

    private final NotificationService notificationService;

    private final NotificationRepository notificationRepository;

    private final SubscriptionRepository subscriptionRepository;

    private final SubscriptionHistoryRepository subscriptionHistoryRepository;

    public static final ZoneId ZONE = ZoneId.of("Asia/Damascus");


    // Runs every day at 03:00:00 AM
    @Transactional
    @Scheduled(cron = "0 0 3 * * *", zone = "Asia/Damascus")
    public void sendExpirationNotifications() {
        List<SubscriptionHistory> latestSubs = subscriptionHistoryRepository.findLatestSubscriptions();

        LocalDate today = LocalDate.now(ZONE);

        List<User> expiringUsers = new ArrayList<>();
        List<User> expiredUsers = new ArrayList<>();

        for (SubscriptionHistory sh : latestSubs) {
            LocalDate endDate = sh.getPaymentDate().toLocalDate().plusMonths(1);
            if (endDate.equals(today.plusDays(1))) {
                expiringUsers.add(sh.getUser());
            } else if (!endDate.isAfter(today)) {
                expiredUsers.add(sh.getUser());
            }
        }

        String expiringTitle = "Subscription Ending Tomorrow";
        var expiringNotification = notificationRepository.findByTitle(expiringTitle).orElse(null);
        if (expiringNotification == null)
            expiringNotification = initializeSubscriptionNotifications(expiringTitle, "Your subscription will expire tomorrow. Renew to keep your access.");

        String expiredTitle = "Subscription Expired";
        var expiredNotification = notificationRepository.findByTitle(expiredTitle).orElse(null);
        if (expiredNotification == null)
            expiredNotification = initializeSubscriptionNotifications(expiredTitle, "Your subscription has expired. Renew to regain access.");

        if (!expiringUsers.isEmpty()) {
            notificationService.sendNotificationToUsers(expiringUsers, expiringNotification);
        }

        if (!expiredUsers.isEmpty()) {
            notificationService.sendNotificationToUsers(expiredUsers, expiredNotification);
            // inActive Expired Users
            for (User item : expiredUsers) {
                Subscription userSubscription = subscriptionRepository.findFirstByUserOrderByJoinedAtDesc(item).orElse(null);
                if (userSubscription != null) {
                    userSubscription.setIsActive(false);
                    subscriptionRepository.save(userSubscription);
                }
            }
        }
    }

    @Transactional
    public Notification initializeSubscriptionNotifications(String title, String content) {
        Notification notification = new Notification();
        notification.setTitle(title);
        notification.setContent(content);
        notification.setCreatedAt(LocalDateTime.now(ZONE));
        notificationRepository.save(notification);
        return notification;
    }

    // for test
    // @Scheduled(cron = "0 * * * * *", zone = "Asia/Damascus")

}
