package com.hyperlink.server.domain.content.application;

import com.hyperlink.server.domain.content.domain.ContentRepository;
import com.hyperlink.server.domain.content.domain.entity.Content;
import com.hyperlink.server.domain.content.dto.ContentResponse;
import com.hyperlink.server.domain.content.dto.RecommendationCompanyResponse;
import com.hyperlink.server.domain.content.dto.SearchResponse;
import com.hyperlink.server.domain.content.exception.ContentNotFoundException;
import com.hyperlink.server.domain.content.infrastructure.ContentRepositoryCustom;
import com.hyperlink.server.domain.memberContent.application.MemberContentService;
import com.hyperlink.server.domain.memberHistory.application.MemberHistoryService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ContentService {

  private final ContentRepository contentRepository;
  private final ContentRepositoryCustom contentRepositoryCustom;
  private final MemberContentService memberContentService;
  private final MemberHistoryService memberHistoryService;

  public int getViewCount(Long contentId) {
    Content content = contentRepository.findById(contentId).orElseThrow(
        ContentNotFoundException::new);

    return content.getViewCount();
  }

  @Transactional
  public void addView(Long memberId, Long contentId) {
    contentRepository.updateViewCount(contentId);
    if (memberId != null) {
      memberHistoryService.insertMemberHistory(memberId, contentId);
    }
  }

  public SearchResponse search(Long memberId, String keyword, Pageable pageable) {
    List<String> keywords = splitSearchKeywords(keyword);
    Slice<Content> searchResultContents = contentRepositoryCustom.searchByTitleContaining(keywords, pageable);

    List<ContentResponse> contentResponses = createContentResponses(memberId, searchResultContents);
    return new SearchResponse(contentResponses, searchResultContents.hasNext(), keyword,
        searchResultContents.getNumberOfElements());
  }

  private List<ContentResponse> createContentResponses(Long memberId, Slice<Content> contents) {
    List<ContentResponse> contentResponses = new ArrayList<>();
    for (Content content : contents) {
      boolean isBookmarked = memberContentService.isBookmarked(memberId, content.getId());
      boolean isLiked = content.getLikeCount() > 0;
      // TODO : 회사 추천 리스트 추가
      List<RecommendationCompanyResponse> recommendationCompanyResponses = new ArrayList<>();

      ContentResponse contentResponse = new ContentResponse(content.getId(), content.getTitle(),
          content.getCreator().getName(), content.getContentImgUrl(),
          content.getLink(), content.getLikeCount(), content.getViewCount(), isBookmarked, isLiked,
          content.getCreatedAt().toString(), recommendationCompanyResponses);
      contentResponses.add(contentResponse);
    }

    return contentResponses;
  }

  private List<String> splitSearchKeywords(String keyword) {
    return Arrays.asList(keyword.split(" "));
  }

}
