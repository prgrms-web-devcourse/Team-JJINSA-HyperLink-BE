package com.hyperlink.server.domain.content.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hyperlink.server.domain.content.application.ContentService;
import com.hyperlink.server.domain.content.dto.ContentEnrollResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ContentConsumer {

  private final ObjectMapper objectMapper;
  private final ContentService contentService;

  @RabbitListener(queues = "${spring.rabbitmq.listener.name}")
  public void handler(String message) throws JsonProcessingException {
    ContentEnrollResponse contentEnrollResponse = objectMapper.readValue(message,
        ContentEnrollResponse.class);
    contentService.insertContent(contentEnrollResponse);
    log.info("GET MESSAGE FROM RABBIT MQ! data : {}", contentEnrollResponse);
  }
}
