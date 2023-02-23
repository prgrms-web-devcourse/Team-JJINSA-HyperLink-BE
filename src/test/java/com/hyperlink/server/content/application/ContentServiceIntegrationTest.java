package com.hyperlink.server.content.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.hyperlink.server.domain.content.application.ContentService;
import com.hyperlink.server.domain.content.domain.ContentRepository;
import com.hyperlink.server.domain.content.domain.entity.Content;
import com.hyperlink.server.domain.content.exception.ContentNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@DisplayName("ContentService 통합 테스트")
public class ContentServiceIntegrationTest {

  @Autowired
  ContentService contentService;
  @Autowired
  ContentRepository contentRepository;

  @Nested
  @DisplayName("조회수 갯수 조회 메서드는")
  class Inquiry {

    @Test
    @DisplayName("성공하면 특정 컨텐츠의 조회수를 리턴한다.")
    void success() {
      Content content = new Content("title", "contentImgUrl", "link");
      contentRepository.save(content);

      content.addInquiry();
      int inquiryCountAfterAdd = content.getInquiry();

      int findInquiry = contentService.getInquiry(content.getId());

      assertThat(findInquiry).isEqualTo(inquiryCountAfterAdd);
    }

    @Test
    @DisplayName("없는 컨텐츠를 요청했을 때 ContentNotFoundException가 발생한다.")
    void fail() {
      assertThrows(ContentNotFoundException.class, () -> {
        contentService.getInquiry(0L);
      });
    }
  }
}
