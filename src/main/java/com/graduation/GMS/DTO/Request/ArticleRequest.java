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

    @NotBlank(message = "title is required")
    @Size(max = 100, message = "Title name must not exceed 100 characters")
    private String title;

    @NotBlank(message = "content is required")
    private String content;

    private Wiki wikiType;

}
