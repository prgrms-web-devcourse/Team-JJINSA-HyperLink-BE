package com.hyperlink.server.domain.dailyBriefing.dto;

import com.hyperlink.server.domain.dailyBriefing.domain.vo.DailyBriefing;
import java.io.Serializable;

public record GetDailyBriefingResponse(
    String standardTime,
    DailyBriefing dailyBriefing
) implements Serializable {

}
