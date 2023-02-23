package com.hyperlink.server.domain.content.controller;

import com.hyperlink.server.domain.content.application.ContentService;
import com.hyperlink.server.domain.content.dto.PatchInquiryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/contents")
public class ContentController {

  private final ContentService contentService;

  @PatchMapping("/{contentId}/inquiry")
  @ResponseStatus(HttpStatus.OK)
  public PatchInquiryResponse addInquiryOfContent(@PathVariable("contentId") long contentId) {
    int inquiryCount = contentService.addInquiryAndGetCount(contentId);
    return new PatchInquiryResponse(inquiryCount);
  }

}
