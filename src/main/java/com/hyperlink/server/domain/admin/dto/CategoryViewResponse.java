package com.hyperlink.server.domain.admin.dto;

import com.hyperlink.server.domain.admin.domain.vo.CategoryAndView;
import java.util.List;

public record CategoryViewResponse(List<CategoryAndView> results, String date) {

  public static CategoryViewResponse of(List<CategoryAndView> results, String date) {
    return new CategoryViewResponse(results, date);
  }
}
