package com.graduation.GMS.Models;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.graduation.GMS.Models.Enums.Gender;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 50, nullable = false)
    private String firstName;

    @Column(length = 50, nullable = false)
    private String lastName;

    @Column(unique = true, length = 50, nullable = false)
    private String email;

    @JsonIgnore
    @Column(nullable = false)
    private String password;

    @Column(length = 25, nullable = false, unique = true)
    private String phoneNumber;

    private Gender gender;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dob;

//    private role tinyint

    @Column(updatable = false, nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @Column(unique = true, nullable = false)
    private String qr;

    // Relations
    @OneToOne(mappedBy = "user", orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private AuthToken authToken;

    @OneToMany(mappedBy = "auditCoach", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Class> classList;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Subscription> subscriptionList;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<User_Program> userProgramList;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PrivateCoach> privateUserList;

    @OneToMany(mappedBy = "coach", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PrivateCoach> privateCoachList;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<User_Notification> userNotificationList;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Schedule> scheduleList;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Attendance> attendanceList;

    @OneToMany(mappedBy = "admin", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Event> eventList;

    @OneToMany(mappedBy = "coach", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DietPlan> dietPlanList;

    @OneToMany(mappedBy = "coach", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Session> sessionList;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<User_Session> userSessionList;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<User_Coach> userList;

    @OneToMany(mappedBy = "coach", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<User_Coach> coachList;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Event_Participant> eventParticipantList;

    @OneToMany(mappedBy = "admin", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Article> articleList;

    @OneToMany(mappedBy = "admin", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FAQ> faqList;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SubscriptionHistory> subscriptionHistoryList;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<User_Workout_Progress> userWorkoutProgressList;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<User_Workout_favorite> userWorkoutFavoriteList;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<User_Diet> userDietList;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Message> userMessages;

    @OneToMany(mappedBy = "coach", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Message> coachMessages;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Session_Attendance> sessionAttendanceList;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HealthInfo> healthInfoList;

}