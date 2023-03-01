package com.hyperlink.server.domain.content.controller;

import com.hyperlink.server.domain.content.application.ContentService;
import com.hyperlink.server.domain.content.dto.PatchInquiryResponse;
import com.hyperlink.server.domain.content.dto.SearchResponse;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/contents")
public class ContentController {

  private final ContentService contentService;

  @PatchMapping("/{contentId}/view")
  @ResponseStatus(HttpStatus.OK)
  public PatchInquiryResponse addViewOfContent(@PathVariable("contentId") long contentId) {
    // TODO : JWT
    Long memberId = 1L;
    contentService.addView(memberId, contentId);
    int viewCount = contentService.getViewCount(contentId);
    return new PatchInquiryResponse(viewCount);
  }

  @GetMapping("/search")
  @ResponseStatus(HttpStatus.OK)
  public SearchResponse search(@RequestParam("keyword") @NotBlank String keyword,
      @RequestParam("page") @NotNull int page,
      @RequestParam("size") @NotNull int size) {
    // TODO : JWT
    Long memberId = 1L;
    Pageable pageable = PageRequest.of(page, size);
    return contentService.search(memberId, keyword, pageable);
  }

}
