//package com.hyperlink.server.content.controller;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.hyperlink.server.domain.category.domain.CategoryRepository;
//import com.hyperlink.server.domain.category.domain.entity.Category;
//import com.hyperlink.server.domain.content.domain.ContentRepository;
//import com.hyperlink.server.domain.content.domain.entity.Content;
//import com.hyperlink.server.domain.content.dto.ContentEnrollResponse;
//import com.hyperlink.server.domain.creator.domain.CreatorRepository;
//import com.hyperlink.server.domain.creator.domain.entity.Creator;
//import java.util.LinkedList;
//import java.util.List;
//import java.util.stream.Collectors;
//import javax.persistence.EntityManager;
//import javax.persistence.EntityTransaction;
//import javax.persistence.PersistenceContext;
//import org.junit.jupiter.api.AfterAll;
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.BeforeAll;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
//import org.junit.jupiter.api.Order;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.TestInstance;
//import org.junit.jupiter.api.TestInstance.Lifecycle;
//import org.junit.jupiter.api.TestMethodOrder;
//import org.springframework.amqp.rabbit.core.RabbitTemplate;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.annotation.Rollback;
//import org.springframework.transaction.annotation.Transactional;
//
//@SpringBootTest
//@Rollback(value = false)
//@TestMethodOrder(OrderAnnotation.class)
//@TestInstance(Lifecycle.PER_CLASS)
//public class ContentRabbitMQIntegrationTest {
//
//  @Autowired
//  RabbitTemplate rabbitTemplate;
//
//  @Autowired
//  ObjectMapper objectMapper;
//
//  @Autowired
//  CategoryRepository categoryRepository;
//
//  @Autowired
//  CreatorRepository creatorRepository;
//
//  @Autowired
//  ContentRepository contentRepository;
//
//  List<Content> allByCreatorName = new LinkedList<>();
//
//  Category develop;
//  Creator naverD2;
//
//  @BeforeAll
//  void setUp() {
//    rabbitTemplate.setExchange("crawling-test");
//    rabbitTemplate.setRoutingKey("hello");
//    rabbitTemplate.setDefaultReceiveQueue("crawling-test-queue");
//  }
//
//  @AfterAll
//  void tearDown() {
//    contentRepository.deleteAllById(allByCreatorName.stream().map(Content::getId)
//        .collect(Collectors.toList()));
//    creatorRepository.delete(naverD2);
//    categoryRepository.delete(develop);
//  }
//
//  @Order(1)
//  @Test
//  @DisplayName("데이터 저장")
//  void saveData() {
//    develop = categoryRepository.save(new Category("develop"));
//    naverD2 = creatorRepository.save(
//        new Creator("naverD2", "profileImgUrl", "description", develop));
//  }
//
//  @Order(2)
//  @Test
//  @DisplayName("RabbitMQ에 데이터를 넣으면 이는 DB로 저장된다.")
//  void rabbitMQSaveToDatabase() throws InterruptedException, JsonProcessingException {
//    ContentEnrollResponse contentEnrollResponse1 = new ContentEnrollResponse(
//        "DEVIEW 2023 D-6! 부스를 소개합니다.",
//        "https://d2nanaver/news/3775781",
//        "https://d2.naver.com/content/images/2023/02/-----------2023-02-06------2-53-17.png",
//        develop.getName(), naverD2.getName());
//
//    ContentEnrollResponse contentEnrollResponse2 = new ContentEnrollResponse(
//        "오프라인으로 돌아온 DEVIEW 2023, 미리보는 참가 신청 방법!",
//        "https://d2nanaver/news/1888051",
//        "https://d2.naver.com/content/images/2023/02/D2-------------_170X120.png",
//        develop.getName(), naverD2.getName());
//
//    ContentEnrollResponse contentEnrollResponse3 = new ContentEnrollResponse(
//        "네이버 검색 SRE 2편 - 측정하지 않으면 개선할 수 없다! SRE KPI 개발기",
//        "https://d2nanaver/helloworld/9231267",
//        "https://d2.naver.com/content/images/2023/01/d2_spring_2nd.png",
//        develop.getName(), naverD2.getName());
//
//    rabbitTemplate.convertAndSend(objectMapper.writeValueAsString(contentEnrollResponse1));
//    rabbitTemplate.convertAndSend(objectMapper.writeValueAsString(contentEnrollResponse2));
//    rabbitTemplate.convertAndSend(objectMapper.writeValueAsString(contentEnrollResponse3));
//
//    Thread.sleep(1000);
//    allByCreatorName = contentRepository.findAllByCreatorName(naverD2.getName());
//    assertEquals(3, allByCreatorName.size());
//  }
//
//}
