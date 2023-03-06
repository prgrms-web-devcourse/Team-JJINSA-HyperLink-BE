package com.hyperlink.server.domain.creator.controller;

import com.hyperlink.server.domain.auth.token.exception.TokenNotExistsException;
import com.hyperlink.server.domain.creator.application.CreatorService;
import com.hyperlink.server.domain.creator.dto.CreatorEnrollRequest;
import com.hyperlink.server.domain.creator.dto.CreatorEnrollResponse;
import com.hyperlink.server.global.config.LoginMemberId;
import java.util.Optional;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CreatorController {

  private final CreatorService creatorService;


  @PostMapping("/admin/creators")
  @ResponseStatus(HttpStatus.CREATED)
  public CreatorEnrollResponse enrollCreator(
      @LoginMemberId Optional<Long> memberId,
      @RequestBody @Valid CreatorEnrollRequest creatorEnrollRequest) {
    memberId.orElseThrow(TokenNotExistsException::new);
    return creatorService.enrollCreator(creatorEnrollRequest);
  }

  @DeleteMapping("/admin/creators/{creatorId}")
  @ResponseStatus(HttpStatus.OK)
  public void deleteCreator(@LoginMemberId Optional<Long> memberId,
      @PathVariable("creatorId") Long creatorId) {
    creatorService.deleteCreator(creatorId);
  }

  @PostMapping("/creators/{creatorId}/not-recommend")
  @ResponseStatus(HttpStatus.OK)
  public void notRecommend(@LoginMemberId Optional<Long> optionalMemberId,
      @PathVariable("creatorId") Long creatorId) {
    Long memberId = optionalMemberId.orElseThrow(TokenNotExistsException::new);
    creatorService.notRecommend(memberId, creatorId);
  }
}
