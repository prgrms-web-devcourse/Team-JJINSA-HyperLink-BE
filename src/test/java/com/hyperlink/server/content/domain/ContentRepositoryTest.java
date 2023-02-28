package com.hyperlink.server.content.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.hyperlink.server.domain.content.domain.ContentRepository;
import com.hyperlink.server.domain.content.domain.entity.Content;
import com.hyperlink.server.domain.content.exception.ContentNotFoundException;
import com.hyperlink.server.domain.creator.domain.CreatorRepository;
import com.hyperlink.server.domain.creator.domain.entity.Creator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
class ContentRepositoryTest {

  @Autowired
  ContentRepository contentRepository;
  @Autowired
  CreatorRepository creatorRepository;

  @Test
  @DisplayName("조회수 update 메소드를 실행하면 조회수가 +1 된다")
  void updateInquiryTest() {
    Creator creator = new Creator("name", "profile", "description");
    Content content = new Content("title", "contentImgUrl", "link", creator);
    int beforeInquiry = content.getViewCount();
    creatorRepository.save(creator);
    contentRepository.save(content);

    contentRepository.updateViewCount(content.getId());

    Content findContent = contentRepository.findById(content.getId())
        .orElseThrow(ContentNotFoundException::new);

    assertThat(findContent.getViewCount()).isEqualTo(beforeInquiry + 1);
  }
}
