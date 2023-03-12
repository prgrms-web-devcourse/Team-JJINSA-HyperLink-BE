package com.hyperlink.server.domain.attentionCategory.dto;

import java.util.List;

public record AttentionCategoryResponse(
    List<String> attentionCategory
) {

  public static AttentionCategoryResponse from(List<String> nameList) {
    return new AttentionCategoryResponse(nameList);
  }
}
