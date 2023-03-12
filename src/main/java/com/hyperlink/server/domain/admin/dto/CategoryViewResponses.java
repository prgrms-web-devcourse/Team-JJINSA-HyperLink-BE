package com.hyperlink.server.domain.admin.dto;

import java.util.List;

public record CategoryViewResponses(List<CategoryViewResponse> weeklyViewCounts, String createdDate) {

}
