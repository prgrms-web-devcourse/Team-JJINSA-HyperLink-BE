package com.hyperlink.server.domain.content.infrastructure;

import static com.hyperlink.server.domain.content.domain.entity.QContent.content;

import com.hyperlink.server.domain.content.domain.entity.Content;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ContentRepositoryCustom {

  private static final int VIEW_COUNT_WEIGHT = 1;
  private static final int LIKE_COUNT_WEIGHT = 3;
  private static final int PAST_DAYS = 5;


  private final JPAQueryFactory queryFactory;

  public Page<Content> searchByTitleContainingOrderByLatest(List<String> keywords,
      Pageable pageable) {
    BooleanBuilder builder = new BooleanBuilder();
    for (String keyword : keywords) {
      builder.or(content.title.contains(keyword));
    }

    List<Content> contents = queryFactory
        .selectFrom(content)
        .where(builder, eqActiveContent())
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .orderBy(content.createdAt.desc())
        .fetch();

    Long size = queryFactory.select(content.count())
        .from(content)
        .where(builder)
        .fetchOne();

    if (size == null) {
      size = 0L;
    }

    return new PageImpl<>(contents, pageable, size);
  }

  public Slice<Content> retrievePopularTrendContentsByCategory(Long categoryId, Pageable pageable) {
    List<Content> contents = queryFactory
        .selectFrom(content)
        .where(eqCategoryId(categoryId), beforeNDays(PAST_DAYS), eqActiveContent())
        .orderBy(popularOrderType(), orderByContentId())
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize() + 1)
        .fetch();

    return getSlice(pageable, contents);
  }

  public Slice<Content> retrieveRecentTrendContentsByCategory(Long categoryId, Pageable pageable) {
    List<Content> contents = queryFactory
        .selectFrom(content)
        .where(eqCategoryId(categoryId), eqActiveContent())
        .orderBy(recentOrderType(), orderByContentId())
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize() + 1)
        .fetch();

    return getSlice(pageable, contents);
  }

  public Slice<Content> retrievePopularTrendContentsForCategories(List<Long> categoryIds,
      Pageable pageable) {
    BooleanBuilder categoryConditionBuilder = new BooleanBuilder();
    for (Long categoryId : categoryIds) {
      categoryConditionBuilder.or(content.category.id.eq(categoryId));
    }

    List<Content> contents = queryFactory
        .selectFrom(content)
        .where(categoryConditionBuilder, beforeNDays(PAST_DAYS), eqActiveContent())
        .orderBy(popularOrderType(), orderByContentId())
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize() + 1)
        .fetch();

    return getSlice(pageable, contents);
  }

  public Slice<Content> retrieveRecentTrendContentsForCategories(List<Long> categoryIds,
      Pageable pageable) {
    BooleanBuilder categoryConditionBuilder = new BooleanBuilder();
    for (Long categoryId : categoryIds) {
      categoryConditionBuilder.or(content.category.id.eq(categoryId));
    }

    List<Content> contents = queryFactory
        .selectFrom(content)
        .where(categoryConditionBuilder, eqActiveContent())
        .orderBy(recentOrderType(), orderByContentId())
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize() + 1)
        .fetch();

    return getSlice(pageable, contents);
  }

  public Slice<Content> retrievePopularContentsByCreator(Long creatorId, Pageable pageable) {
    List<Content> contents = queryFactory
        .selectFrom(content)
        .where(eqCreatorId(creatorId), eqActiveContent())
        .orderBy(popularOrderType(), orderByContentId())
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize() + 1)
        .fetch();

    return getSlice(pageable, contents);
  }

  public Slice<Content> retrieveRecentContentsByCreator(Long creatorId, Pageable pageable) {
    List<Content> contents = queryFactory
        .selectFrom(content)
        .where(eqCreatorId(creatorId), eqActiveContent())
        .orderBy(recentOrderType(), orderByContentId())
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize() + 1)
        .fetch();

    return getSlice(pageable, contents);
  }

  public Slice<Content> retrieveRecentContentsSubscribedCreatorsByCategoryId(
      List<Long> subscribedCreatorIds, Long categoryId, Pageable pageable) {
    BooleanBuilder subscribedCreatorConditionBuilder = new BooleanBuilder();
    for (Long creatorId : subscribedCreatorIds) {
      subscribedCreatorConditionBuilder.or(content.creator.id.eq(creatorId));
    }

    List<Content> contents = queryFactory
        .selectFrom(content)
        .where(subscribedCreatorConditionBuilder, eqCategoryId(categoryId), eqActiveContent())
        .orderBy(recentOrderType(), orderByContentId())
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize() + 1)
        .fetch();

    return getSlice(pageable, contents);
  }

  public Slice<Content> retrieveRecentContentsForAllSubscribedCreators(
      List<Long> subscribedCreatorIds, Pageable pageable) {
    BooleanBuilder subscribedCreatorConditionBuilder = new BooleanBuilder();
    for (Long creatorId : subscribedCreatorIds) {
      subscribedCreatorConditionBuilder.or(content.creator.id.eq(creatorId));
    }

    List<Content> contents = queryFactory
        .selectFrom(content)
        .where(subscribedCreatorConditionBuilder, eqActiveContent())
        .orderBy(recentOrderType(), orderByContentId())
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize() + 1)
        .fetch();

    return getSlice(pageable, contents);
  }

  private SliceImpl<Content> getSlice(Pageable pageable, List<Content> contents) {
    boolean hasNext = false;
    if (contents.size() > pageable.getPageSize()) {
      contents.remove(pageable.getPageSize());
      hasNext = true;
    }
    return new SliceImpl<>(contents, pageable, hasNext);
  }

  private OrderSpecifier<Long> orderByContentId() {
    return content.id.asc();
  }

  private OrderSpecifier<Integer> popularOrderType() {
    return content.viewCount.multiply(VIEW_COUNT_WEIGHT)
        .add(content.likeCount.multiply(LIKE_COUNT_WEIGHT)).desc();
  }

  private OrderSpecifier<LocalDateTime> recentOrderType() {
    return content.createdAt.desc();
  }

  private BooleanExpression eqCreatorId(Long creatorId) {
    return content.creator.id.eq(creatorId);
  }

  private BooleanExpression eqCategoryId(Long categoryId) {
    return content.category.id.eq(categoryId);
  }

  private BooleanExpression beforeNDays(long day) {
    return content.createdAt.after(LocalDateTime.now().minusDays(day));
  }

  private BooleanExpression eqActiveContent() {
    return content.isViewable.eq(true);
  }
}
