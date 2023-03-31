package com.hyperlink.server.content.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.hyperlink.server.TestConfig;
import com.hyperlink.server.domain.category.domain.CategoryRepository;
import com.hyperlink.server.domain.category.domain.entity.Category;
import com.hyperlink.server.domain.content.domain.ContentRepository;
import com.hyperlink.server.domain.content.domain.entity.Content;
import com.hyperlink.server.domain.content.exception.ContentNotFoundException;
import com.hyperlink.server.domain.content.infrastructure.ContentRepositoryCustom;
import com.hyperlink.server.domain.creator.domain.CreatorRepository;
import com.hyperlink.server.domain.creator.domain.entity.Creator;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Import(TestConfig.class)
class ContentRepositoryTest {

  @Autowired
  ContentRepository contentRepository;
  @Autowired
  CreatorRepository creatorRepository;
  @Autowired
  CategoryRepository categoryRepository;
  @Autowired
  JPAQueryFactory jpaQueryFactory;
  ContentRepositoryCustom contentRepositoryCustom;

  @BeforeEach
  void setUp() {
    contentRepositoryCustom = new ContentRepositoryCustom(jpaQueryFactory);
  }

  @Test
  @DisplayName("조회수 update 메소드를 실행하면 조회수가 +1 된다")
  void updateInquiryTest() {
    Category category = new Category("개발test");
    Creator creator = new Creator("name", "profile", "description", category);
    Content content = new Content("title", "contentImgUrl", "link", creator, category);
    int beforeInquiry = content.getViewCount();
    categoryRepository.save(category);
    creatorRepository.save(creator);
    contentRepository.save(content);

    contentRepository.updateViewCount(content.getId());

    Content findContent = contentRepository.findById(content.getId())
        .orElseThrow(ContentNotFoundException::new);

    assertThat(findContent.getViewCount()).isEqualTo(beforeInquiry + 1);
  }

  @Nested
  @DisplayName("컨텐츠 검색 메서드는")
  class Search {
    @Test
    @DisplayName("is_viewable이 false인 컨텐츠는 검색 결과에 제외시켜야 한다.")
    void excludeIsViewableFalseTest() {
      String contentTitle = "이 컨텐츠는 검색이 되면 안됩니다";
      Category category = new Category("개발test");
      Creator creator = new Creator("name", "profile", "description", category);
      Content content = new Content(contentTitle, "contentImgUrl", "link", creator, category);
      content.makeViewable(false);
      categoryRepository.save(category);
      creatorRepository.save(creator);
      contentRepository.save(content);

      Page<Content> contents = contentRepositoryCustom.searchByTitleContainingOrderByLatest(
          List.of(contentTitle), PageRequest.of(0, 10));
      long totalElements = contents.getTotalElements();

      assertThat(contents.getContent()).isEmpty();
      assertThat(totalElements).isZero();
    }
  }
}
