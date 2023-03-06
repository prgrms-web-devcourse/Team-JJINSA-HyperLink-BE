package com.hyperlink.server.domain.content.controller;

import com.hyperlink.server.domain.auth.token.exception.TokenNotExistsException;
import com.hyperlink.server.domain.content.application.ContentService;
import com.hyperlink.server.domain.content.dto.GetContentsCommonResponse;
import com.hyperlink.server.domain.content.dto.PatchInquiryResponse;
import com.hyperlink.server.domain.content.dto.SearchResponse;
import com.hyperlink.server.domain.content.exception.CategoryAndCreatorIdConstraintViolationException;
import com.hyperlink.server.domain.member.exception.MemberNotFoundException;
import com.hyperlink.server.global.config.LoginMemberId;
import java.util.Optional;
import javax.validation.ConstraintViolationException;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
public class ContentController {

  private final ContentService contentService;

  @PatchMapping("/contents/{contentId}/view")
  @ResponseStatus(HttpStatus.OK)
  public PatchInquiryResponse addViewOfContent(@PathVariable("contentId") long contentId) {
    // TODO : JWT
    Long memberId = 1L;
    contentService.addView(memberId, contentId);
    int viewCount = contentService.getViewCount(contentId);
    return new PatchInquiryResponse(viewCount);
  }

  @PostMapping("/contents/search")
  @ResponseStatus(HttpStatus.OK)
  public SearchResponse search(@LoginMemberId Optional<Long> memberId,
      @RequestParam("keyword") @NotBlank String keyword,
      @RequestParam("page") @NotNull int page,
      @RequestParam("size") @NotNull int size) {
    if (memberId.isEmpty()) {
      throw new TokenNotExistsException();
    }
    Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
    return contentService.search(memberId.get(), keyword, pageable);
  }

  @GetMapping("/contents")
  @ResponseStatus(HttpStatus.OK)
  public GetContentsCommonResponse getContents(@LoginMemberId Optional<Long> optionalMemberId,
      @RequestParam(value = "category", required = false) String category,
      @RequestParam(value = "creatorId", required = false) @Min(1) Long creatorId,
      @RequestParam("page") @NotNull int page,
      @RequestParam("size") @NotNull int size,
      @RequestParam("sort") @NotBlank String sort) {

    validateCategoryAndCreatorId(category, creatorId);
    Long memberId = optionalMemberId.orElse(null);

    if (category != null) {
      return contentService.retrieveTrendContents(memberId, category, sort,
          PageRequest.of(page, size));
    }
    return contentService.retrieveCreatorContents(memberId, creatorId, sort,
        PageRequest.of(page, size));
  }

  @GetMapping("/contents/all")
  @ResponseStatus(HttpStatus.OK)
  public GetContentsCommonResponse getAllCategoriesContents(@LoginMemberId Optional<Long> optionalMemberId,
      @RequestParam("page") @NotNull int page,
      @RequestParam("size") @NotNull int size,
      @RequestParam("sort") @NotBlank String sort) {
    Long memberId = optionalMemberId.orElse(null);
    return contentService.retrieveTrendAllCategoriesContents(
        memberId, sort, PageRequest.of(page, size));
  }

  @PostMapping("/admin/contents/{contentId}/activate")
  @ResponseStatus(HttpStatus.OK)
  public void activateContent(@LoginMemberId Optional<Long> optionalMemberId,
      @PathVariable("contentId") Long contentId) {
    Long memberId = optionalMemberId.orElseThrow(MemberNotFoundException::new);
    contentService.activateContent(contentId);
  }

  private void validateCategoryAndCreatorId(String category, Long creatorId) {
    if (checkCategoryAndCreatorIdBothNull(category, creatorId) ||
        checkCategoryAndCreatorIdBothNotNull(category, creatorId)) {
      throw new CategoryAndCreatorIdConstraintViolationException();
    }
  }

  private boolean checkCategoryAndCreatorIdBothNull(String category, Long creatorId) {
    return category == null && creatorId == null;
  }

  private boolean checkCategoryAndCreatorIdBothNotNull(String category, Long creatorId) {
    return category != null && creatorId != null;
  }
}
