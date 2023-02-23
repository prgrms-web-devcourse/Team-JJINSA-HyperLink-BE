package com.hyperlink.server.content.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.hyperlink.server.domain.content.application.ContentService;
import com.hyperlink.server.domain.content.domain.ContentRepository;
import com.hyperlink.server.domain.content.domain.entity.Content;
import com.hyperlink.server.domain.content.exception.ContentNotFoundException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
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

  @Nested
  @DisplayName("조회수 갯수 조회 메서드는")
  class Inquiry {

    @Test
    @DisplayName("성공하면 특정 컨텐츠의 조회수를 리턴한다.")
    void success() {
      Content content = new Content("title", "contentImgUrl", "link");
      contentRepository.save(content);
      int inquiryCountBeforeAdd = content.getInquiry();

      contentService.addInquiryAndGetCount(content.getId());

      int findInquiry = contentService.getInquiry(content.getId());

      assertThat(findInquiry).isEqualTo(inquiryCountBeforeAdd + 1);
    }

    @Test
    @DisplayName("없는 컨텐츠를 요청했을 때 ContentNotFoundException가 발생한다.")
    void fail() {
      assertThrows(ContentNotFoundException.class, () -> {
        contentService.getInquiry(0L);
      });
    }
  }

  @TestInstance(Lifecycle.PER_CLASS)
  @TestMethodOrder(OrderAnnotation.class)
  @Nested
  @DisplayName("조회수 추가 메서드는")
  class AddInquiryAndGetCount {

    Content content;
    final int memberCount = 3;
    int beforeInquiry;

    @Order(1)
    @Rollback(value = false)
    @Test
    @DisplayName("3명의 사용자가 동시에 조회수 추가를 요청했을 때")
    void manyRequestInquiry() throws InterruptedException {
      final CountDownLatch countDownLatch = new CountDownLatch(memberCount);

      content = new Content("title", "contentImgUrl", "link");
      content = contentRepository.save(content);
      Long contentId = content.getId();
      beforeInquiry = content.getInquiry();

      List<Thread> workers = Stream.generate(() -> new Thread(new Worker(countDownLatch, contentId)))
          .limit(memberCount)
          .toList();
      workers.forEach(Thread::start);
      countDownLatch.await(5, TimeUnit.SECONDS);
    }

    @Order(2)
    @Test
    @DisplayName("조회수가 +3 처리된다.")
    void addInquiry() {
      Content findContent = contentRepository.findById(content.getId())
          .orElseThrow(ContentNotFoundException::new);
      assertThat(findContent.getInquiry()).isEqualTo(beforeInquiry + memberCount);
    }

    @Order(3)
    @Rollback(value = false)
    @Test
    void tearDownContent() {
      contentRepository.deleteById(content.getId());
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
        contentService.addInquiryAndGetCount(contentId);
        countDownLatch.countDown();
      }
    }

  }
}
