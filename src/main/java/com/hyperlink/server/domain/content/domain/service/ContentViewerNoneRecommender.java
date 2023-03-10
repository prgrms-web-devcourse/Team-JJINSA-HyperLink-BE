package com.hyperlink.server.domain.content.domain.service;

import com.hyperlink.server.domain.content.dto.ContentViewerRecommendationResponse;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ContentViewerNoneRecommender implements ContentViewerRecommender{

  @Override
  public List<ContentViewerRecommendationResponse> getContentViewerRecommendationResponse(Long contentId) {
    return Collections.emptyList();
  }
}
