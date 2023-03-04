package com.hyperlink.server.domain.subscription.controller;

import com.hyperlink.server.domain.auth.token.exception.TokenNotExistsException;
import com.hyperlink.server.domain.subscription.dto.SubscribeResponse;
import com.hyperlink.server.domain.subscription.application.SubscriptionService;
import com.hyperlink.server.global.config.LoginMemberId;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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


}
