package com.hyperlink.server.domain.memberContent.dto;

import com.hyperlink.server.domain.content.dto.ContentResponse;
import java.util.List;

public record BookmarkPageResponse(
    List<ContentResponse> contents,
    boolean hasNext) {

}
