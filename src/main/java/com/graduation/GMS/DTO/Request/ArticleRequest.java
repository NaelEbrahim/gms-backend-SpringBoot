package com.graduation.GMS.DTO.Request;

import com.graduation.GMS.Models.Enums.Wiki;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ArticleRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 100, message = "Title name must not exceed 100 characters")
    private String title;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String content;

    private String category;

    private Wiki wikiType;

}
