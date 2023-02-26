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

  public int getInquiry(Long contentId) {
    Content content = contentRepository.findById(contentId).orElseThrow(
        ContentNotFoundException::new);

    return content.getViewCount();
  }

  @Transactional
  public int addInquiryAndGetCount(Long contentId) {
    contentRepository.updateInquiryCount(contentId);
    return getInquiry(contentId);
  }
}
