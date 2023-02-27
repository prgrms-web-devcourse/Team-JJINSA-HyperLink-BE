package com.hyperlink.server.domain.content.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hyperlink.server.domain.content.dto.ContentCreationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ContentConsumer {

  private final ObjectMapper objectMapper;

  @RabbitListener(queues="crawling-test-queue")
  public void handler(String message) throws JsonProcessingException {
    ContentCreationResponse post = objectMapper.readValue(message, ContentCreationResponse.class);

  }
}
