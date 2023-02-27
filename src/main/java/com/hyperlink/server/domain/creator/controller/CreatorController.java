package com.hyperlink.server.domain.creator.controller;

import com.hyperlink.server.domain.creator.application.CreatorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/creators")
public class CreatorController {

  private final CreatorService creatorService;

  @PostMapping("/{creatorId}/not-recommend")
  @ResponseStatus(HttpStatus.OK)
  public void notRecommend(@PathVariable("creatorId") Long creatorId) {
    // TODO : JWT
    Long memberId = 1L;
    creatorService.notRecommend(memberId, creatorId);
  }
}
