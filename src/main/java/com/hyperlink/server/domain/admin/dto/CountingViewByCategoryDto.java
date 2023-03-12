package com.hyperlink.server.domain.admin.dto;

public interface CountingViewByCategoryDto {
  Long getCategoryId();
  String getCategoryName();
  int getViewCount();
}
