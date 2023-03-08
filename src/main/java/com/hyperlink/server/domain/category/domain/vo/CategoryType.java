package com.hyperlink.server.domain.category.domain.vo;

import com.hyperlink.server.domain.category.exception.CategoryNotFoundException;
import java.util.stream.Stream;

public enum CategoryType {

  DEVELOP("develop"),
  BEAUTY("beauty"),
  FINANCE("finance");

  final String requestParamName;

  CategoryType(String requestParamName) {
    this.requestParamName = requestParamName;
  }

  public String getRequestParamName() {
    return requestParamName;
  }

  public static CategoryType of(String name) {
    return Stream.of(CategoryType.values())
        .filter(categoryType -> categoryType.requestParamName.equals(name))
        .findFirst()
        .orElseThrow(CategoryNotFoundException::new);
  }
}
