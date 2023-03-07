package com.hyperlink.server.domain.dailyBriefing.dto;

import java.io.Serializable;

public record StatisticsByCategoryResponse(
    String categoryName,
    long count,
    int ranking
) implements Serializable {

}
