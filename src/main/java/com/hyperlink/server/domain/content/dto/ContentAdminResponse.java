package com.hyperlink.server.domain.content.dto;

import com.hyperlink.server.domain.content.domain.entity.Content;

public record ContentAdminResponse(Long contentId, String title, String link) {
  public static ContentAdminResponse from(Content content) {
    return new ContentAdminResponse(content.getId(), content.getTitle(), content.getLink());
  }
}
