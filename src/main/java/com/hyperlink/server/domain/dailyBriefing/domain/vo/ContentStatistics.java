package com.hyperlink.server.domain.dailyBriefing.domain.vo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@Getter
@NoArgsConstructor
@RedisHash(value = "contentStatistics", timeToLive = 691200)    // TTL : 8일
public class ContentStatistics {

  @Id
  private String date;
  private int contentIncrease;

  public ContentStatistics(String date, int contentIncrease) {
    this.date = date;
    this.contentIncrease = contentIncrease;
  }

  @Override
  public String toString() {
    return "ContentStatistics{" +
        "date='" + date + '\'' +
        ", contentIncrease=" + contentIncrease +
        '}';
  }
}
