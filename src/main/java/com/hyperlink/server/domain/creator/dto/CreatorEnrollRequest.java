package com.hyperlink.server.domain.creator.dto;

import com.hyperlink.server.domain.category.domain.entity.Category;
import com.hyperlink.server.domain.creator.domain.entity.Creator;
import javax.validation.constraints.NotBlank;

public record CreatorEnrollRequest(
    @NotBlank String name,
    String profileImgUrl,
    @NotBlank String description,
    @NotBlank String categoryName) {
  public static Creator toCreator(CreatorEnrollRequest creatorEnrollRequest, Category category) {
    return new Creator(creatorEnrollRequest.name(), creatorEnrollRequest.profileImgUrl(),
        creatorEnrollRequest.description(), category);
  }
}
