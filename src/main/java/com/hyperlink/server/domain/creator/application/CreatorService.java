package com.hyperlink.server.domain.creator.application;

import com.hyperlink.server.domain.category.domain.CategoryRepository;
import com.hyperlink.server.domain.category.domain.entity.Category;
import com.hyperlink.server.domain.category.exception.CategoryNotFoundException;
import com.hyperlink.server.domain.creator.domain.CreatorRepository;
import com.hyperlink.server.domain.creator.domain.entity.Creator;
import com.hyperlink.server.domain.creator.dto.CreatorEnrollRequest;
import com.hyperlink.server.domain.creator.dto.CreatorEnrollResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreatorService {

  private final CreatorRepository creatorRepository;
  private final CategoryRepository categoryRepository;

  @Transactional
  public CreatorEnrollResponse enrollCreator(CreatorEnrollRequest creatorEnrollRequest) {
    Category category = categoryRepository.findByName(creatorEnrollRequest.categoryName())
        .orElseThrow(CategoryNotFoundException::new);
    Creator creator = CreatorEnrollRequest.toCreator(creatorEnrollRequest, category);
    Creator savedCreator = creatorRepository.save(creator);
    return CreatorEnrollResponse.from(savedCreator);
  }
}
