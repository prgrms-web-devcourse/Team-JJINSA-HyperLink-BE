package com.hyperlink.server.domain.content.application;

import com.hyperlink.server.domain.content.domain.ContentRepository;
import com.hyperlink.server.domain.content.domain.entity.Content;
import com.hyperlink.server.domain.content.exception.ContentNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ContentService {

  private final ContentRepository contentRepository;

  public int getViewCount(Long contentId) {
    Content content = contentRepository.findById(contentId).orElseThrow(
        ContentNotFoundException::new);

    return content.getViewCount();
  }

  @Transactional
  public void addView(Long contentId) {
    contentRepository.updateViewCount(contentId);
  }
}
