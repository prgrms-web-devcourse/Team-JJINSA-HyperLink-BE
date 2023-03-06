package com.hyperlink.server.domain.content.application;

import com.hyperlink.server.domain.category.domain.CategoryRepository;
import com.hyperlink.server.domain.category.domain.entity.Category;
import com.hyperlink.server.domain.category.exception.CategoryNotFoundException;
import com.hyperlink.server.domain.common.ContentDtoFactoryService;
import com.hyperlink.server.domain.content.domain.ContentRepository;
import com.hyperlink.server.domain.content.domain.entity.Content;
import com.hyperlink.server.domain.content.dto.ContentEnrollResponse;
import com.hyperlink.server.domain.content.dto.GetContentsCommonResponse;
import com.hyperlink.server.domain.content.dto.SearchResponse;
import com.hyperlink.server.domain.content.exception.ContentNotFoundException;
import com.hyperlink.server.domain.content.infrastructure.ContentRepositoryCustom;
import com.hyperlink.server.domain.creator.domain.CreatorRepository;
import com.hyperlink.server.domain.creator.domain.entity.Creator;
import com.hyperlink.server.domain.creator.exception.CreatorNotFoundException;
import com.hyperlink.server.domain.memberContent.application.MemberContentService;
import com.hyperlink.server.domain.memberHistory.application.MemberHistoryService;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContentService {

  private final ContentRepository contentRepository;
  private final CategoryRepository categoryRepository;
  private final CreatorRepository creatorRepository;
  private final ContentRepositoryCustom contentRepositoryCustom;
  private final MemberContentService memberContentService;
  private final ContentDtoFactoryService contentDtoFactoryService;
  private final MemberHistoryService memberHistoryService;

  public int getViewCount(Long contentId) {
    Content content = contentRepository.findById(contentId).orElseThrow(
        ContentNotFoundException::new);

    return content.getViewCount();
  }

  @Transactional
  public void addView(Optional<Long> optionalMemberId, Long contentId) {
    contentRepository.updateViewCount(contentId);
    optionalMemberId.ifPresent(
        memberId -> memberHistoryService.insertMemberHistory(memberId, contentId));
  }

  public SearchResponse search(Long memberId, String keyword, Pageable pageable) {
    List<String> keywords = splitSearchKeywords(keyword);
    Slice<Content> searchResultContents = contentRepositoryCustom.searchByTitleContainingOrderByLatest(keywords,
        pageable);

    GetContentsCommonResponse contentResponses = contentDtoFactoryService.createContentResponses(
        memberId, searchResultContents.getContent(), searchResultContents.hasNext());
    return new SearchResponse(contentResponses, keyword,
        searchResultContents.getNumberOfElements());
  }

  private List<String> splitSearchKeywords(String keyword) {
    return Arrays.asList(keyword.split(" "));
  }

  @Transactional
  public Long insertContent(ContentEnrollResponse contentEnrollResponse) {
    Creator creator = creatorRepository.findByName(contentEnrollResponse.creatorName())
        .orElseThrow(CreatorNotFoundException::new);
    Category category = categoryRepository.findByName(contentEnrollResponse.categoryName())
        .orElseThrow(CategoryNotFoundException::new);

    Content content = ContentEnrollResponse.toContent(contentEnrollResponse, creator, category);
    boolean exist = contentRepository.existsByLink(content.getLink());
    if (exist) {
      log.info("이미 존재하는 컨텐츠입니다. {}", contentEnrollResponse);
      return -1L;
    }
    contentRepository.save(content);
    return content.getId();
  }
}
