package com.hyperlink.server.domain.content.application;

import com.hyperlink.server.domain.attentionCategory.domain.AttentionCategoryRepository;
import com.hyperlink.server.domain.category.domain.CategoryRepository;
import com.hyperlink.server.domain.category.domain.entity.Category;
import com.hyperlink.server.domain.category.exception.CategoryNotFoundException;
import com.hyperlink.server.domain.common.ContentDtoFactoryService;
import com.hyperlink.server.domain.content.domain.ContentRepository;
import com.hyperlink.server.domain.content.domain.entity.Content;
import com.hyperlink.server.domain.content.dto.ContentAdminResponse;
import com.hyperlink.server.domain.content.dto.ContentAdminResponses;
import com.hyperlink.server.domain.content.dto.ContentEnrollResponse;
import com.hyperlink.server.domain.content.dto.GetContentsCommonResponse;
import com.hyperlink.server.domain.content.dto.SearchResponse;
import com.hyperlink.server.domain.content.exception.ContentNotFoundException;
import com.hyperlink.server.domain.content.exception.InvalidSortException;
import com.hyperlink.server.domain.content.infrastructure.ContentRepositoryCustom;
import com.hyperlink.server.domain.creator.domain.CreatorRepository;
import com.hyperlink.server.domain.creator.domain.entity.Creator;
import com.hyperlink.server.domain.creator.exception.CreatorNotFoundException;
import com.hyperlink.server.domain.memberHistory.application.MemberHistoryService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContentService {
  private static final int PLUS_ONE_FOR_CURRENT_PAGE = 1;

  private final ContentRepository contentRepository;
  private final CategoryRepository categoryRepository;
  private final CreatorRepository creatorRepository;
  private final AttentionCategoryRepository attentionCategoryRepository;
  private final ContentRepositoryCustom contentRepositoryCustom;
  private final ContentDtoFactoryService contentDtoFactoryService;
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
    Slice<Content> searchResultContents = contentRepositoryCustom.searchByTitleContainingOrderByLatest(
        keywords,
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

  public GetContentsCommonResponse retrieveTrendContents(Long memberId, String categoryName,
      String sort, Pageable pageable) {
    Category category = categoryRepository.findByName(categoryName)
        .orElseThrow(CategoryNotFoundException::new);

    Slice<Content> contents = switch (sort) {
      case "recent" ->
          contentRepositoryCustom.retrieveRecentTrendContentsByCategory(category.getId(), pageable);
      case "popular" ->
          contentRepositoryCustom.retrievePopularTrendContentsByCategory(category.getId(),
              pageable);
      default -> throw new InvalidSortException();
    };

    return contentDtoFactoryService.createContentResponses(memberId, contents.getContent(),
        contents.hasNext());
  }

  public GetContentsCommonResponse retrieveTrendAllCategoriesContents(Long memberId,
      String sort, Pageable pageable) {
    List<Long> categoryIds = new ArrayList<>();
    if (memberId == null) {
      categoryIds = categoryRepository.findAllCategoryIds();
    } else {
      categoryIds = attentionCategoryRepository.findAttentionCategoryIdsByMemberId(memberId);
    }

    Slice<Content> contents = switch (sort) {
      case "recent" ->
          contentRepositoryCustom.retrieveRecentTrendContentsForCategories(categoryIds, pageable);
      case "popular" ->
          contentRepositoryCustom.retrievePopularTrendContentsForCategories(categoryIds, pageable);
      default -> throw new InvalidSortException();
    };

    return contentDtoFactoryService.createContentResponses(memberId, contents.getContent(),
        contents.hasNext());
  }

  public GetContentsCommonResponse retrieveCreatorContents(Long memberId, Long creatorId,
      String sort, Pageable pageable) {
    Slice<Content> contents = switch (sort) {
      case "recent" -> contentRepositoryCustom.retrieveRecentContentsByCreator(creatorId, pageable);
      case "popular" ->
          contentRepositoryCustom.retrievePopularContentsByCreator(creatorId, pageable);
      default -> throw new InvalidSortException();
    };

    return contentDtoFactoryService.createContentResponses(memberId, contents.getContent(),
        contents.hasNext());
  }

  @Transactional
  public void activateContent(Long contentId) {
    Content content = contentRepository.findById(contentId)
        .orElseThrow(ContentNotFoundException::new);
    content.makeViewable(true);
  }


  public ContentAdminResponses retrieveInactivatedContents(Pageable pageable) {
    Page<Content> inactivatedContentsPage = contentRepository.findInactivatedContents(pageable);
    List<Content> inactivatedContents = inactivatedContentsPage.getContent();
    List<ContentAdminResponse> inactivatedContentAdminResponses = inactivatedContents.stream()
        .map(ContentAdminResponse::from).toList();
    return ContentAdminResponses.of(inactivatedContentAdminResponses,
        inactivatedContentsPage.getNumber() + PLUS_ONE_FOR_CURRENT_PAGE,
        inactivatedContentsPage.getTotalPages());
  }

  @Transactional
  public void addLike(Long contentId) {
    Content foundContent = contentRepository.selectForUpdate(contentId)
        .orElseThrow(ContentNotFoundException::new);
    foundContent.addLike();
  }

  @Transactional
  public void subTractLike(Long contentId) {
    Content foundContent = contentRepository.selectForUpdate(contentId)
        .orElseThrow(ContentNotFoundException::new);
    foundContent.subtractLike();

  }

  @Transactional
  public void deleteContentsById(Long contentId) {
    boolean exists = contentRepository.existsById(contentId);
    if (!exists) {
      throw new ContentNotFoundException();
    }
    contentRepository.deleteById(contentId);
  }
}
