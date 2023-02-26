package com.hyperlink.server.content.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.hyperlink.server.domain.content.application.ContentService;
import com.hyperlink.server.domain.content.domain.ContentRepository;
import com.hyperlink.server.domain.content.domain.entity.Content;
import com.hyperlink.server.domain.content.exception.ContentNotFoundException;
import com.hyperlink.server.domain.creator.domain.CreatorRepository;
import com.hyperlink.server.domain.creator.domain.entity.Creator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@DisplayName("ContentService 통합 테스트")
public class ContentServiceIntegrationTest {

  @Autowired
  ContentService contentService;
  @Autowired
  ContentRepository contentRepository;
  @Autowired
  CreatorRepository creatorRepository;

  @Nested
  @DisplayName("조회수 갯수 조회 메서드는")
  class View {

    @Test
    @DisplayName("성공하면 특정 컨텐츠의 조회수를 리턴한다.")
    void success() {
      Creator creator = new Creator("name", "profile", "description");
      Content content = new Content("title", "contentImgUrl", "link", creator);
      creatorRepository.save(creator);
      contentRepository.save(content);
      int inquiryCountBeforeAdd = content.getViewCount();

      contentService.addView(content.getId());

      int findInquiry = contentService.getViewCount(content.getId());

      assertThat(findInquiry).isEqualTo(inquiryCountBeforeAdd + 1);
    }

    @Test
    @DisplayName("없는 컨텐츠를 요청했을 때 ContentNotFoundException가 발생한다.")
    void fail() {
      assertThrows(ContentNotFoundException.class, () -> {
        contentService.getViewCount(0L);
      });
    }
  }

  @TestMethodOrder(OrderAnnotation.class)
  @Nested
  @DisplayName("조회수 추가 메서드는")
  class AddViewAndGetCount {

    static Long contentId;
    final int memberCount = 3;
    int beforeViewCount;

    @Order(1)
    @Rollback(value = false)
    @Test
    @DisplayName("3명의 사용자가 동시에 조회수 추가를 요청했을 때")
    void manyRequestView() throws InterruptedException {
      final CountDownLatch countDownLatch = new CountDownLatch(memberCount);

      Creator creator = new Creator("name", "profile", "description");
      Content content = new Content("title", "contentImgUrl", "link", creator);
      creatorRepository.save(creator);
      content = contentRepository.save(content);
      contentId = content.getId();
      beforeViewCount = content.getViewCount();

      List<Thread> workers = Stream.generate(() -> new Thread(new Worker(countDownLatch, contentId)))
          .limit(memberCount)
          .toList();
      workers.forEach(Thread::start);
      countDownLatch.await();
    }

    @Order(2)
    @Test
    @DisplayName("조회수가 +3 처리된다.")
    void addView() {
      Content findContent = contentRepository.findById(contentId)
          .orElseThrow(ContentNotFoundException::new);
      assertThat(findContent.getViewCount()).isEqualTo(beforeViewCount + memberCount);
    }

    @Order(3)
    @Rollback(value = false)
    @Test
    void tearDownContent() {
      contentRepository.deleteById(contentId);
    }

    private class Worker implements Runnable {

      private CountDownLatch countDownLatch;
      private Long contentId;

      public Worker(CountDownLatch countDownLatch, Long contentId) {
        this.countDownLatch = countDownLatch;
        this.contentId = contentId;
      }

      @Override
      public void run() {
        countDownLatch.countDown();
        contentService.addView(contentId);
      }
    }

  }
}
