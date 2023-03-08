package com.hyperlink.server.domain.dailyBriefing.dto;

import java.io.Serializable;
import java.util.List;

public record DailyBriefing(
    int memberIncrease,
    int viewIncrease,
    List<StatisticsByCategoryResponse> viewByCategories,
    int contentIncrease,
    List<StatisticsByCategoryResponse> memberCountByAttentionCategories
) implements Serializable {

}
