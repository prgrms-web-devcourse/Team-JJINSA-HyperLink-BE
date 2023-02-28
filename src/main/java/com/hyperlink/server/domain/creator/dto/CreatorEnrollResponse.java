package com.hyperlink.server.domain.creator.dto;

import com.hyperlink.server.domain.creator.domain.entity.Creator;

public record CreatorEnrollResponse(Long id, String name, String profileImgUrl, String description,
                                    String categoryName) {
  public static CreatorEnrollResponse from(Creator creator) {
    return new CreatorEnrollResponse(creator.getId(), creator.getName(), creator.getProfileImgUrl(),
        creator.getDescription(), creator.getCategoryName());
  }
}
