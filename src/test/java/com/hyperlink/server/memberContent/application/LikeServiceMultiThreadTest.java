package com.hyperlink.server.memberContent.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.hyperlink.server.domain.category.domain.CategoryRepository;
import com.hyperlink.server.domain.category.domain.entity.Category;
import com.hyperlink.server.domain.content.application.ContentService;
import com.hyperlink.server.domain.content.domain.ContentRepository;
import com.hyperlink.server.domain.content.domain.entity.Content;
import com.hyperlink.server.domain.content.exception.ContentNotFoundException;
import com.hyperlink.server.domain.creator.domain.CreatorRepository;
import com.hyperlink.server.domain.creator.domain.entity.Creator;
import com.hyperlink.server.domain.member.domain.Career;
import com.hyperlink.server.domain.member.domain.CareerYear;
import com.hyperlink.server.domain.member.domain.MemberRepository;
import com.hyperlink.server.domain.member.domain.entity.Member;
import com.hyperlink.server.domain.memberContent.application.LikeService;
import com.hyperlink.server.domain.memberContent.domain.MemberContentRepository;
import com.hyperlink.server.domain.memberContent.dto.LikeClickRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@TestInstance(Lifecycle.PER_CLASS)
@Slf4j
@SpringBootTest
@Transactional
@DisplayName("LikeService 통합테스트")
public class LikeServiceMultiThreadTest {

  @Autowired
  ContentService contentService;
  @Autowired
  LikeService likeService;
  @Autowired
  MemberContentRepository memberContentRepository;
  @Autowired
  MemberRepository memberRepository;
  @Autowired
  ContentRepository contentRepository;
  @Autowired
  CategoryRepository categoryRepository;
  @Autowired
  CreatorRepository creatorRepository;

  Content content;
  Category category;
  Creator creator;
  List<Member> members = new ArrayList<>();

  @BeforeAll
  void setup() {
    category = new Category("개발");
    creator = new Creator("name", "profile", "description", category);
    content = new Content("title", "contentImgUrl", "link", creator, category);
    categoryRepository.save(category);
    creatorRepository.save(creator);
    contentRepository.save(content);

    for (int i = 0; i < 10; i++) {

      Member newMember = new Member("rldnd1234@naver.com" + i, "Chocho" + i, Career.DEVELOP,
          CareerYear.MORE_THAN_TEN,
          "localhost" + i, 1995, "man" + i);
      Member savedMember = memberRepository.save(newMember);
      members.add(savedMember);
    }
  }

  @AfterAll
  void delete() {
    memberContentRepository.deleteAll();
    memberRepository.deleteAll();
    contentRepository.delete(content);
    creatorRepository.delete(creator);
    categoryRepository.delete(category);
  }

  @Test
  @DisplayName("여러명이 동시에 좋아요를 눌러도 의도한 값을 얻을 수 있다.")
  void multiThreadLikeClickTest() throws InterruptedException {
    int threadCount = 10;
    ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
    CountDownLatch latch = new CountDownLatch(threadCount);

    LikeClickRequest likeClickRequest = new LikeClickRequest(true);

    for (int i = 0; i < threadCount; i++) {
      final int index = i;
      executorService.execute(() -> {
        likeService.clickLike(members.get(index).getId(), content.getId(), likeClickRequest);
        latch.countDown();
      });
    }
    latch.await();

    Content foundContent = contentRepository.findById(content.getId())
        .orElseThrow(ContentNotFoundException::new);
    assertThat(foundContent.getLikeCount()).isEqualTo(threadCount);
    log.info("test 실패");
  }
}
