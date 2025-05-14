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

    private String category;

    private Wiki wikiType;

    private LocalDateTime createdAt;

    private LocalDateTime lastModifiedAt;

}
