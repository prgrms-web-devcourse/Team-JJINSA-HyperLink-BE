package com.hyperlink.server.domain.admin.domain.vo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class CategoryAndView {
  private final String categoryName;
  private final int viewCount;

  @JsonCreator
  public CategoryAndView(
      @JsonProperty("categoryName") String categoryName,
      @JsonProperty("viewCount") int viewCount) {
    this.categoryName = categoryName;
    this.viewCount = viewCount;
  }
}
