package com.hyperlink.server.domain.attentionCategory.application;

import com.hyperlink.server.domain.attentionCategory.domain.AttentionCategoryRepository;
import com.hyperlink.server.domain.attentionCategory.domain.entity.AttentionCategory;
import com.hyperlink.server.domain.attentionCategory.dto.AttentionCategoryResponse;
import com.hyperlink.server.domain.category.domain.CategoryRepository;
import com.hyperlink.server.domain.category.domain.entity.Category;
import com.hyperlink.server.domain.category.exception.CategoryNotFoundException;
import com.hyperlink.server.domain.member.domain.MemberRepository;
import com.hyperlink.server.domain.member.domain.entity.Member;
import com.hyperlink.server.domain.member.exception.MemberNotFoundException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@Service
public class AttentionCategoryService {

  private final AttentionCategoryRepository attentionCategoryRepository;
  private final MemberRepository memberRepository;
  private final CategoryRepository categoryRepository;

  public AttentionCategoryService(AttentionCategoryRepository attentionCategoryRepository,
      MemberRepository memberRepository, CategoryRepository categoryRepository) {
    this.attentionCategoryRepository = attentionCategoryRepository;
    this.memberRepository = memberRepository;
    this.categoryRepository = categoryRepository;
  }

  public AttentionCategoryResponse getAttentionCategory(Long memberId) {
    List<String> attentionCategoryNames = attentionCategoryRepository.findAttentionCategoryNamesByMemberId(
        memberId);
    return AttentionCategoryResponse.from(attentionCategoryNames);
  }

  @Transactional
  public AttentionCategoryResponse changeAttentionCategory(Long memberId,
      List<String> attentionCategories) {

    Member foundMember = memberRepository.findById(memberId)
        .orElseThrow(MemberNotFoundException::new);

    attentionCategoryRepository.deleteAttentionCategoriesByMember(foundMember);

    List<String> savedAttentionCategory = new ArrayList<>();
    attentionCategories.stream().forEach(categoryName -> {
      Category category = categoryRepository.findByName(categoryName)
          .orElseThrow(CategoryNotFoundException::new);
      attentionCategoryRepository.save(new AttentionCategory(foundMember, category));
      savedAttentionCategory.add(category.getName());
    });
    return AttentionCategoryResponse.from(savedAttentionCategory);
  }
}

