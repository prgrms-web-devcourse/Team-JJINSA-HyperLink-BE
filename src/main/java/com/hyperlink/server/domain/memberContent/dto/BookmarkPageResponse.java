package com.hyperlink.server.domain.memberContent.dto;

import com.hyperlink.server.domain.content.dto.BookMarkedContentPageResponse;
import java.util.List;

public record BookmarkPageResponse(
    List<BookMarkedContentPageResponse> contents,
    boolean hasNext) {

}
