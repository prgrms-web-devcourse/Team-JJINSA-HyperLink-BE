package com.hyperlink.server.content.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.hyperlink.server.domain.category.domain.CategoryRepository;
import com.hyperlink.server.domain.category.domain.entity.Category;
import com.hyperlink.server.domain.content.domain.ContentRepository;
import com.hyperlink.server.domain.content.domain.entity.Content;
import com.hyperlink.server.domain.content.exception.ContentNotFoundException;
import com.hyperlink.server.domain.content.infrastructure.ContentRepositoryCustom;
import com.hyperlink.server.domain.creator.domain.CreatorRepository;
import com.hyperlink.server.domain.creator.domain.entity.Creator;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
class ContentRepositoryTest {

  @Autowired
  ContentRepository contentRepository;
  @Autowired
  CreatorRepository creatorRepository;
  @Autowired
  CategoryRepository categoryRepository;
  @Autowired
  ContentRepositoryCustom contentRepositoryCustom;

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

  @Test
  void fsfd() {
    List<String> adf = List.of("코딩", "당근", "효");
    List<Content> content = contentRepositoryCustom.searchByTitleContainingOrderByLatest(adf,
        PageRequest.of(0, 100)).getContent();

    for (Content content1 : content) {
      System.out.println(content1.toString());
    }
  }
}
