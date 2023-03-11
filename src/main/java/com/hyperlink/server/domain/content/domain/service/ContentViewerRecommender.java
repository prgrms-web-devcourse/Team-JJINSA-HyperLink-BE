package com.hyperlink.server.domain.content.domain.service;

import com.hyperlink.server.domain.content.dto.ContentViewerRecommendationResponse;
import java.util.List;

public interface ContentViewerRecommender {

  List<ContentViewerRecommendationResponse> getContentViewerRecommendationResponse(Long contentId);

}
