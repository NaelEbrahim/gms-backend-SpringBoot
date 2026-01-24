package com.graduation.GMS.Models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.graduation.GMS.Models.Enums.Wiki;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Article {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true, nullable = false)
    private String title;

    @Lob
    @Column(nullable = false,columnDefinition = "MEDIUMTEXT")
    private String content;


    @Enumerated(EnumType.STRING)
    private Wiki wikiType;

    @Column(updatable = false, nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @Column(insertable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastModifiedAt;

    @ManyToOne
    @JoinColumn(name = "admin_id")
    private User admin;

}