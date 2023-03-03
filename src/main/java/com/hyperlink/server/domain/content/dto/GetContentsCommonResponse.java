package com.hyperlink.server.domain.content.dto;

import java.util.List;

public record GetContentsCommonResponse(
    List<ContentResponse> contents,
    boolean hasNext
) {

}
