package com.graduation.GMS.DTO.Response;

import com.graduation.GMS.Models.Enums.Wiki;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ArticleResponse {
    private Integer id;

    private UserResponse admin;

    private String title;

    private String content;

    private Wiki wikiType;

    private LocalDateTime createdAt;

    private LocalDateTime lastModifiedAt;

    private Integer minReadTime ;

    public ArticleResponse(Integer id, UserResponse admin, String title, String content, Wiki wikiType, LocalDateTime createdAt, LocalDateTime lastModifiedAt) {
        this.id = id;
        this.admin = admin;
        this.title = title;
        this.content = content;
        this.wikiType = wikiType;
        this.createdAt = createdAt;
        this.lastModifiedAt = lastModifiedAt;
        this.minReadTime = calculateReadTime(this.content);
    }

    public static int calculateReadTime(String content) {
        if (content == null || content.trim().isEmpty()) {
            return 0;
        }
        String[] words = content.trim().split("\\s+");
        int wordCount = words.length;
        double timeInMinutes = (double) wordCount / 60;
        return (int) Math.ceil(timeInMinutes);
    }

}
