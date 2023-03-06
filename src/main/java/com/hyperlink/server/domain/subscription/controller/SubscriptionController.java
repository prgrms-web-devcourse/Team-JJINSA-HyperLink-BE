package com.hyperlink.server.domain.subscription.controller;

import com.hyperlink.server.domain.auth.token.exception.TokenNotExistsException;
import com.hyperlink.server.domain.content.dto.GetContentsCommonResponse;
import com.hyperlink.server.domain.subscription.dto.SubscribeResponse;
import com.hyperlink.server.domain.subscription.application.SubscriptionService;
import com.hyperlink.server.global.config.LoginMemberId;
import java.util.Optional;
import javax.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SubscriptionController {

  private final SubscriptionService subscriptionService;

  @PostMapping("/creators/{creatorId}/subscribe")
  @ResponseStatus(HttpStatus.OK)
  public SubscribeResponse subscribeOrUnsubscribeCreator(@LoginMemberId Optional<Long> memberId,
      @PathVariable("creatorId") Long creatorId) {
    Long loginMemberId = memberId.orElseThrow(TokenNotExistsException::new);
    return subscriptionService.subscribeOrUnsubscribeCreator(loginMemberId, creatorId);
  }

  @GetMapping("/subscriptions/contents")
  @ResponseStatus(HttpStatus.OK)
  public GetContentsCommonResponse retrieveSubscribedContents(@LoginMemberId Optional<Long> optionalMemberId,
      @RequestParam("category") @NotNull String category,
      @RequestParam("page") @NotNull int page,
      @RequestParam("size") @NotNull int size) {
    Long loginMemberId = optionalMemberId.orElseThrow(TokenNotExistsException::new);
    return subscriptionService.retrieveSubscribedCreatorsContentsByCategoryId(loginMemberId, category, PageRequest.of(page, size));
  }

  @GetMapping("/subscriptions/contents/all")
  @ResponseStatus(HttpStatus.OK)
  public GetContentsCommonResponse retrieveSubscribedContents(@LoginMemberId Optional<Long> optionalMemberId,
      @RequestParam("page") @NotNull int page,
      @RequestParam("size") @NotNull int size) {
    Long loginMemberId = optionalMemberId.orElseThrow(TokenNotExistsException::new);
    return subscriptionService.retrieveSubscribedCreatorsContentsForAllCategories(loginMemberId, PageRequest.of(page, size));
  }

}
