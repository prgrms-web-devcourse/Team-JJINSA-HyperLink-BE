package com.hyperlink.server.domain.dailyBriefing.dto;

import java.io.Serializable;

public record GetDailyBriefingResponse(
    String standardTime,
    DailyBriefing dailyBriefing
) implements Serializable {

}
