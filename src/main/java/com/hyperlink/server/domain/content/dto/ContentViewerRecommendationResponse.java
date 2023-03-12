package com.hyperlink.server.domain.content.dto;

import com.hyperlink.server.domain.content.exception.InvalidAgeException;
import com.hyperlink.server.domain.content.exception.InvalidGenderException;
import com.hyperlink.server.domain.memberContent.dto.ContentViewerAgeAndGenderRecommendDto;
import com.hyperlink.server.domain.memberContent.dto.ContentViewerCompanyRecommendDto;

public record ContentViewerRecommendationResponse(
    String bannerName,
    String bannerLogoImgUrl
) {

  public static ContentViewerRecommendationResponse from(
      ContentViewerCompanyRecommendDto contentViewerCompanyRecommendDto) {
    return new ContentViewerRecommendationResponse(
        contentViewerCompanyRecommendDto.getCompanyName(),
        contentViewerCompanyRecommendDto.getLogoImgUrl());
  }

  public static ContentViewerRecommendationResponse from(
      ContentViewerAgeAndGenderRecommendDto contentViewerAgeAndGenderRecommendDto) {
    StringBuffer sb = new StringBuffer();
    StringBuffer bannerName = sb.append(convertAge(contentViewerAgeAndGenderRecommendDto.getAge()))
        .append(" ")
        .append(convertGender(contentViewerAgeAndGenderRecommendDto.getGender()));
    return new ContentViewerRecommendationResponse(sb.toString(), "");
  }

  private static String convertAge(String age) {
    return switch (age) {
      case "10" -> "10대";
      case "20" -> "20대";
      case "30" -> "30대";
      case "40" -> "40대 이상";
      default -> throw new InvalidAgeException();
    };
  }

  private static String convertGender(String gender) {
    return switch (gender) {
      case "man" -> "남성";
      case "woman" -> "여성";
      default -> throw new InvalidGenderException();
    };
  }
}
