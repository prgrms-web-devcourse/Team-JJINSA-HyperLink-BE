package com.hyperlink.server.domain.content.dto;

import java.util.List;

public record ContentAdminResponses(List<ContentAdminResponse> contents, int currentPage,
                                    int totalPage) {
  public static ContentAdminResponses of(List<ContentAdminResponse> contents, int currentPage,
      int totalPage) {
    return new ContentAdminResponses(contents, currentPage, totalPage);
  }
}
