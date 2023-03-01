package com.hyperlink.server.domain.attentionCategory.application;

import com.hyperlink.server.domain.attentionCategory.domain.AttentionCategoryRepository;
import com.hyperlink.server.domain.attentionCategory.domain.entity.AttentionCategory;
import com.hyperlink.server.domain.category.domain.CategoryRepository;
import com.hyperlink.server.domain.category.domain.entity.Category;
import com.hyperlink.server.domain.category.exception.CategoryNotFoundException;
import com.hyperlink.server.domain.member.domain.entity.Member;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@Service
public class AttentionCategoryService {

  private final AttentionCategoryRepository attentionCategoryRepository;
  private final CategoryRepository categoryRepository;

  public AttentionCategoryService(AttentionCategoryRepository attentionCategoryRepository,
      CategoryRepository categoryRepository) {
    this.attentionCategoryRepository = attentionCategoryRepository;
    this.categoryRepository = categoryRepository;
  }

  @Transactional
  public void setAttentionCategory(Member member, List<String> attentionCategory) {
    attentionCategory.stream().forEach(categoryName -> {
      Category category = categoryRepository.findByName(categoryName)
          .orElseThrow(CategoryNotFoundException::new);
      attentionCategoryRepository.save(new AttentionCategory(member, category));
    });
  }
}
