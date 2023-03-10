package com.hyperlink.server.domain.content.domain.service;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ContentViewerRecommenderFactory {

  private static final String COMPANY_RECOMMENDER = "contentViewerCompanyRecommender";
  private static final String AGE_AND_GENDER_RECOMMENDER = "contentViewerAgeAndGenderRecommender";
  private static final String NONE_RECOMMENDER = "contentViewerNoneRecommender";

  private final Map<String, ContentViewerRecommender> contentViewerRecommenders;

  public ContentViewerRecommender getContentViewerRecommender(String categoryName) {
    return switch (categoryName) {
      case "develop" -> contentViewerRecommenders.get(COMPANY_RECOMMENDER);
      case "beauty" -> contentViewerRecommenders.get(AGE_AND_GENDER_RECOMMENDER);
      default -> contentViewerRecommenders.get(NONE_RECOMMENDER);
    };
  }
}
