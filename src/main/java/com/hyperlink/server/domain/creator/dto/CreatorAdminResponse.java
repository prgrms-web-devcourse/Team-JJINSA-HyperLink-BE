package com.hyperlink.server.domain.creator.dto;

import com.hyperlink.server.domain.creator.domain.entity.Creator;

public record CreatorAdminResponse(Long creatorId, String name, String description, String categoryName) {
  public static CreatorAdminResponse from(Creator creator) {
    return new CreatorAdminResponse(creator.getId(), creator.getName(), creator.getDescription(),
        creator.getCategoryName());
  }
}
