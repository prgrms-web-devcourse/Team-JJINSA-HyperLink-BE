package com.hyperlink.server.domain.notRecommendCreator.domain;

import com.hyperlink.server.domain.notRecommendCreator.domain.entity.NotRecommendCreator;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotRecommendCreatorRepository extends JpaRepository<NotRecommendCreator, Long> {

  List<NotRecommendCreator> findByMemberId(Long memberId);
}
