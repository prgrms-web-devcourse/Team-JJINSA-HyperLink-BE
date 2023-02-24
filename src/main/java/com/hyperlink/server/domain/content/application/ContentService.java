package com.hyperlink.server.domain.content.application;

import com.hyperlink.server.domain.content.domain.ContentRepository;
import com.hyperlink.server.domain.content.domain.entity.Content;
import com.hyperlink.server.domain.content.exception.ContentNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ContentService {

  private final ContentRepository contentRepository;

  public int getInquiry(Long contentId) {
    Content content = contentRepository.findById(contentId).orElseThrow(
        ContentNotFoundException::new);

    return content.getInquiry();
  }

}
