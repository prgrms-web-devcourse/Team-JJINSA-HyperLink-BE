package com.hyperlink.server.domain.creator.controller;

import com.hyperlink.server.domain.auth.token.exception.TokenNotExistsException;
import com.hyperlink.server.domain.creator.application.CreatorService;
import com.hyperlink.server.domain.creator.dto.CreatorAdminResponses;
import com.hyperlink.server.domain.creator.dto.CreatorEnrollRequest;
import com.hyperlink.server.domain.creator.dto.CreatorEnrollResponse;
import com.hyperlink.server.domain.creator.dto.CreatorResponse;
import com.hyperlink.server.domain.creator.dto.CreatorsRetrievalResponse;
import com.hyperlink.server.domain.member.exception.MemberNotFoundException;
import com.hyperlink.server.global.config.LoginMemberId;
import java.util.Optional;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
public class CreatorController {

  private final CreatorService creatorService;

  @GetMapping("/admin/creators")
  @ResponseStatus(HttpStatus.OK)
  public CreatorAdminResponses retrieveAdminCreators(
      @LoginMemberId Optional<Long> optionalMemberId,
      @RequestParam("page") @Min(0) @NotNull int page,
      @RequestParam("size") @Min(1) @NotNull int size
  ) {
    optionalMemberId.orElseThrow(MemberNotFoundException::new);
    return creatorService.retrieveCreatorsForAdmin(PageRequest.of(page, size));
  }


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
    memberId.orElseThrow(TokenNotExistsException::new);
    creatorService.deleteCreator(creatorId);
  }

  @PostMapping("/creators/{creatorId}/not-recommend")
  @ResponseStatus(HttpStatus.OK)
  public void notRecommend(@LoginMemberId Optional<Long> optionalMemberId,
      @PathVariable("creatorId") Long creatorId) {
    Long memberId = optionalMemberId.orElseThrow(TokenNotExistsException::new);
    creatorService.notRecommend(memberId, creatorId);
  }

  @GetMapping("/creators")
  @ResponseStatus(HttpStatus.OK)
  public CreatorsRetrievalResponse retrieveCreatorByCategory(
      @LoginMemberId Optional<Long> optionalMemberId,
      @RequestParam("category") @NotNull String category,
      @RequestParam("page") @NotNull int page,
      @RequestParam("size") @NotNull int size) {
    Long memberId = optionalMemberId.orElse(null);
    return creatorService.getCreatorsByCategory(memberId, category, PageRequest.of(page, size));
  }

  @GetMapping("/creators/{creatorId}")
  @ResponseStatus(HttpStatus.OK)
  public CreatorResponse getCreatorDetail(
      @LoginMemberId Optional<Long> optionalMemberId,
      @PathVariable("creatorId") Long creatorId) {
    Long memberId = optionalMemberId.orElse(null);
    return creatorService.getCreatorDetail(memberId, creatorId);
  }
}
