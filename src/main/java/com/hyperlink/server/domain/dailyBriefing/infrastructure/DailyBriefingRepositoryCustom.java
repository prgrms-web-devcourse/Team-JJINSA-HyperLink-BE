package com.hyperlink.server.domain.dailyBriefing.infrastructure;

import static com.hyperlink.server.domain.attentionCategory.domain.entity.QAttentionCategory.attentionCategory;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class DailyBriefingRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  public Optional<Long> getMemberCountByAttentionCategories(Long categoryId) {
    return Optional.ofNullable(queryFactory.select(attentionCategory.count())
        .from(attentionCategory)
        .where(eqCategoryId(categoryId))
        .fetchOne());
  }

  private BooleanExpression eqCategoryId(Long categoryId) {
    return attentionCategory.category.id.eq(categoryId);
  }
}
