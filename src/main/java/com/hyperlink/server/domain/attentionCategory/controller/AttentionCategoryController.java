package com.hyperlink.server.domain.attentionCategory.controller;

import com.hyperlink.server.domain.attentionCategory.application.AttentionCategoryService;
import com.hyperlink.server.domain.attentionCategory.dto.AttentionCategoryRequest;
import com.hyperlink.server.domain.attentionCategory.dto.AttentionCategoryResponse;
import com.hyperlink.server.domain.member.exception.MemberNotFoundException;
import com.hyperlink.server.global.config.LoginMemberId;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AttentionCategoryController {

  private final AttentionCategoryService attentionCategoryService;

  public AttentionCategoryController(AttentionCategoryService attentionCategoryService) {
    this.attentionCategoryService = attentionCategoryService;
  }

  @PutMapping("/attention-category/update")
  @ResponseStatus(HttpStatus.OK)
  public AttentionCategoryResponse changeAttentionCategory(
      @LoginMemberId Optional<Long> optionalId,
      @RequestBody AttentionCategoryRequest attentionCategoryRequest) {
    Long memberId = optionalId.orElseThrow(MemberNotFoundException::new);
    return attentionCategoryService.changeAttentionCategory(memberId,
        attentionCategoryRequest.attentionCategory());
  }
}
