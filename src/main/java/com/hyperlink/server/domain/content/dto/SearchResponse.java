package com.hyperlink.server.domain.content.dto;

import java.util.List;

public record SearchResponse(
    List<ContentResponse> contents,
    boolean hasNext,
    String keyword,
    int resultCount
) {

}
