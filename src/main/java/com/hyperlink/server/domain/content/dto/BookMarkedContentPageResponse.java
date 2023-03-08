package com.hyperlink.server.domain.content.dto;

import com.hyperlink.server.domain.content.domain.entity.Content;
import java.time.LocalDateTime;

public record BookMarkedContentPageResponse(
    Long contentId,
    String title,
    String contentImgUrl,
    String link,
    int likeCount,
    int viewCount,
    LocalDateTime createdAt
) {

  public static BookMarkedContentPageResponse from(Content content) {
    return new BookMarkedContentPageResponse(content.getId(), content.getTitle(),
        content.getContentImgUrl(), content.getLink(),
        content.getLikeCount(), content.getViewCount(), content.getCreatedAt());
  }
}
