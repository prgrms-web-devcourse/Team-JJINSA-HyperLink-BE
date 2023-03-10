package com.hyperlink.server.domain.admin.controller;

import com.hyperlink.server.domain.admin.application.AdminService;
import com.hyperlink.server.domain.admin.dto.CategoryViewResponses;
import com.hyperlink.server.global.config.LoginMemberId;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AdminController {

  private final AdminService adminService;

  @GetMapping("/admin/dashboard/all-category/view")
  @ResponseStatus(HttpStatus.OK)
  public CategoryViewResponses getDailyBriefingForAdmin(@LoginMemberId Optional<Long> optionalMemberId) {
    return adminService.getCategoryView().orElseGet(adminService::countViewCountByCategory);
  }


}
