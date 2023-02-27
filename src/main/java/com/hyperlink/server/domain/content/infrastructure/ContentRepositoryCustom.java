package com.hyperlink.server.domain.content.infrastructure;

import static com.hyperlink.server.domain.content.domain.entity.QContent.content;

import com.hyperlink.server.domain.content.domain.entity.Content;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ContentRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  public Slice<Content> searchByTitleContaining(List<String> keywords, Pageable pageable) {
    BooleanBuilder builder = new BooleanBuilder();
    for (String keyword : keywords) {
      builder.or(content.title.contains(keyword));
    }

    List<Content> contents = queryFactory
        .selectFrom(content)
        .where(builder)
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

    long size = queryFactory.selectFrom(content)
        .where(builder)
        .fetch().size();

    return new PageImpl<>(contents, pageable, size);
  }
}
