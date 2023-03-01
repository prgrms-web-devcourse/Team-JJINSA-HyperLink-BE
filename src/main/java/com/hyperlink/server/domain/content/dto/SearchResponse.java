package com.hyperlink.server.domain.content.dto;

public record SearchResponse(
    GetContentsCommonResponse getContentsCommonResponse,
    String keyword,
    int resultCount
) {

}
