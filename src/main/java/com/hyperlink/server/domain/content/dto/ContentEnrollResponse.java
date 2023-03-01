package com.hyperlink.server.domain.content.dto;

import com.hyperlink.server.domain.category.domain.entity.Category;
import com.hyperlink.server.domain.content.domain.entity.Content;
import com.hyperlink.server.domain.creator.domain.entity.Creator;

public record ContentEnrollResponse(String title, String link, String contentImgLink,
                                    String categoryName, String creatorName) {

  public static Content toContent(ContentEnrollResponse contentEnrollResponse, Creator creator,
      Category category) {
    return new Content(contentEnrollResponse.title(), contentEnrollResponse.contentImgLink(),
        contentEnrollResponse.link(), creator, category);
  }
}
