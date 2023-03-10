package com.hyperlink.server.domain.common;

import static com.hyperlink.server.domain.memberContent.domain.entity.MemberContentActionType.LIKE;

import com.hyperlink.server.domain.content.domain.entity.Content;
import com.hyperlink.server.domain.content.domain.service.ContentViewerRecommenderFactory;
import com.hyperlink.server.domain.content.dto.ContentResponse;
import com.hyperlink.server.domain.content.dto.GetContentsCommonResponse;
import com.hyperlink.server.domain.content.dto.ContentViewerRecommendationResponse;
import com.hyperlink.server.domain.memberContent.application.BookmarkService;
import com.hyperlink.server.domain.memberContent.domain.MemberContentRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ContentDtoFactoryService {

  private final BookmarkService bookmarkService;
  private final ContentViewerRecommenderFactory contentViewerRecommenderFactory;
  private final MemberContentRepository memberContentRepository;

  /**
   * @description memberId에 null이 들어올 수 있음 null이라면 isBookmarked에서 false 반환
   */
  public GetContentsCommonResponse createContentResponses(Long memberId, List<Content> contents,
      boolean hasNext) {
    List<ContentResponse> contentResponses = new ArrayList<>();
    for (Content content : contents) {
      boolean isBookmarked = bookmarkService.isBookmarked(memberId, content.getId());
      boolean isLiked = (memberId == null) ? false
          : memberContentRepository.existsMemberContentByMemberIdAndContentIdAndType(
              memberId, content.getId(), LIKE.getTypeNumber());
      List<ContentViewerRecommendationResponse> contentViewerRecommendationResponses = getContentViewerRecommendationResponse(
          content.getCategory().getName(), content.getId()
      );

      ContentResponse contentResponse = new ContentResponse(content.getId(), content.getTitle(),
          content.getCreator().getName(), content.getCreator().getId(), content.getContentImgUrl(),
          content.getLink(), content.getLikeCount(), content.getViewCount(), isBookmarked, isLiked,
          content.getCreatedAt().toString(), contentViewerRecommendationResponses);
      contentResponses.add(contentResponse);
    }

    return new GetContentsCommonResponse(contentResponses, hasNext);
  }

  public List<ContentViewerRecommendationResponse> getContentViewerRecommendationResponse(
      String categoryName, Long contentId) {
    return contentViewerRecommenderFactory.getContentViewerRecommender(categoryName)
        .getContentViewerRecommendationResponse(contentId);
  }
}
