package com.hyperlink.server.domain.memberContent.controller;

import com.hyperlink.server.domain.auth.token.exception.TokenNotExistsException;
import com.hyperlink.server.domain.memberContent.application.MemberContentService;
import com.hyperlink.server.global.config.LoginMemberId;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MemberContentController {

  private final MemberContentService memberContentService;

  @PostMapping("/bookmark/{contentId}")
  @ResponseStatus(HttpStatus.OK)
  public void includeCreateOrDeleteBookmark(@LoginMemberId Optional<Long> optionalMemberId,
      @PathVariable("contentId") Long contentId, @RequestParam("type") boolean type) {
    Long memberId = optionalMemberId.orElseThrow(TokenNotExistsException::new);
    if (type) {
      memberContentService.createBookmark(memberId, contentId);
    } else {
      memberContentService.deleteBookmark(memberId, contentId);
    }
  }
}
