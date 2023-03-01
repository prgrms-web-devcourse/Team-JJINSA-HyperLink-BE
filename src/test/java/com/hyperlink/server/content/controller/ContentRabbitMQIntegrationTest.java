package com.hyperlink.server.content.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hyperlink.server.domain.category.domain.CategoryRepository;
import com.hyperlink.server.domain.content.domain.ContentRepository;
import com.hyperlink.server.domain.content.domain.entity.Content;
import com.hyperlink.server.domain.content.dto.ContentEnrollResponse;
import com.hyperlink.server.domain.creator.domain.CreatorRepository;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ContentRabbitMQIntegrationTest {

  @Autowired
  RabbitTemplate rabbitTemplate;

  @Autowired
  ObjectMapper objectMapper;

  @Autowired
  CategoryRepository categoryRepository;

  @Autowired
  CreatorRepository creatorRepository;

  @Autowired
  ContentRepository contentRepository;

  List<Content> allByCreatorName = new LinkedList<>();

  @BeforeEach
  void setUp() {
    rabbitTemplate.setExchange("crawling-test");
    rabbitTemplate.setRoutingKey("hello");
    rabbitTemplate.setDefaultReceiveQueue("crawling-test-queue");
  }

  @AfterEach
  void tearDown() {
    contentRepository.deleteAllById(allByCreatorName.stream().map(Content::getId)
        .collect(Collectors.toList()));
  }

  @Test
  @DisplayName("RabbitMQ에 데이터를 넣으면 이는 DB로 저장된다.")
  void rabbitMQSaveToDatabase() throws InterruptedException, JsonProcessingException {
    String naverCreatorName = "ybCreatorName(삭제x)";
    ContentEnrollResponse contentEnrollResponse1 = new ContentEnrollResponse(
        "DEVIEW 2023 D-6! 부스를 소개합니다.",
        "https://d2.naver.com/news/3775781",
        "https://d2.naver.com/content/images/2023/02/-----------2023-02-06------2-53-17.png",
        "developCategoryTest(삭제x)", naverCreatorName);

    ContentEnrollResponse contentEnrollResponse2 = new ContentEnrollResponse(
        "오프라인으로 돌아온 DEVIEW 2023, 미리보는 참가 신청 방법!",
        "https://d2.naver.com/news/1888051",
        "https://d2.naver.com/content/images/2023/02/D2-------------_170X120.png",
        "developCategoryTest(삭제x)", naverCreatorName);

    ContentEnrollResponse contentEnrollResponse3 = new ContentEnrollResponse(
        "네이버 검색 SRE 2편 - 측정하지 않으면 개선할 수 없다! SRE KPI 개발기",
        "https://d2.naver.com/helloworld/9231267",
        "https://d2.naver.com/content/images/2023/01/d2_spring_2nd.png",
        "developCategoryTest(삭제x)", naverCreatorName);

    rabbitTemplate.convertAndSend(objectMapper.writeValueAsString(contentEnrollResponse1));
    rabbitTemplate.convertAndSend(objectMapper.writeValueAsString(contentEnrollResponse2));
    rabbitTemplate.convertAndSend(objectMapper.writeValueAsString(contentEnrollResponse3));

    Thread.sleep(5000);
    allByCreatorName = contentRepository.findAllByCreatorName(naverCreatorName);
    assertEquals(3, allByCreatorName.size());
  }

}
