package com.hyperlink.server.domain.memberHistory.controller;

import com.hyperlink.server.domain.auth.token.exception.TokenNotExistsException;
import com.hyperlink.server.domain.content.dto.GetContentsCommonResponse;
import com.hyperlink.server.domain.memberHistory.application.MemberHistoryService;
import com.hyperlink.server.global.config.LoginMemberId;
import java.util.Optional;
import javax.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
public class MemberHistoryController {

  private final MemberHistoryService memberHistoryService;

  @GetMapping("/history")
  @ResponseStatus(HttpStatus.OK)
  public GetContentsCommonResponse getAllHistory(@LoginMemberId Optional<Long> memberId,
      @RequestParam("page") @NotNull int page,
      @RequestParam("size") @NotNull int size) {
    if (memberId.isEmpty()) {
      throw new TokenNotExistsException();
    }
    Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
    return memberHistoryService.getAllHistory(memberId.get(), pageable);
  }
}
