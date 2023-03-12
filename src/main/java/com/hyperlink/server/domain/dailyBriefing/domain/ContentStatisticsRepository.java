package com.hyperlink.server.domain.dailyBriefing.domain;

import com.hyperlink.server.domain.dailyBriefing.domain.vo.ContentStatistics;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;

public interface ContentStatisticsRepository extends CrudRepository<ContentStatistics, String> {

  @Override
  Optional<ContentStatistics> findById(String date);
}
