package com.hyperlink.server.domain.memberContent.controller;

import com.hyperlink.server.domain.auth.token.exception.TokenNotExistsException;
import com.hyperlink.server.domain.memberContent.application.BookmarkService;
import com.hyperlink.server.domain.memberContent.application.LikeService;
import com.hyperlink.server.domain.memberContent.dto.LikeClickRequest;
import com.hyperlink.server.domain.memberContent.dto.LikeClickResponse;
import com.hyperlink.server.global.config.LoginMemberId;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MemberContentController {

  private final BookmarkService bookmarkService;
  private final LikeService likeService;

  @PostMapping("/bookmark/{contentId}")
  @ResponseStatus(HttpStatus.OK)
  public void includeCreateOrDeleteBookmark(@LoginMemberId Optional<Long> optionalMemberId,
      @PathVariable("contentId") Long contentId, @RequestParam("type") boolean type) {
    Long memberId = optionalMemberId.orElseThrow(TokenNotExistsException::new);
    if (type) {
      bookmarkService.createBookmark(memberId, contentId);
    } else {
      bookmarkService.deleteBookmark(memberId, contentId);
    }
  }

  @PostMapping("/like/{contentId}")
  @ResponseStatus(HttpStatus.OK)
  public LikeClickResponse clickLike(@LoginMemberId Optional<Long> optionalMemberId,
      @PathVariable("contentId") Long contentId, @RequestBody LikeClickRequest likeClickRequest) {
    Long memberId = optionalMemberId.orElseThrow(TokenNotExistsException::new);
    return likeService.clickLike(memberId, contentId, likeClickRequest);
  }
}
