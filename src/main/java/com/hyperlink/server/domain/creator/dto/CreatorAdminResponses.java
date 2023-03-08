package com.hyperlink.server.domain.creator.dto;

import java.util.List;

public record CreatorAdminResponses(List<CreatorAdminResponse> creators, int currentPage,
                                    int totalPage) {

}
