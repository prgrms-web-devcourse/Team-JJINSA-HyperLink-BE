package com.hyperlink.server.attentionCategory.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hyperlink.server.domain.attentionCategory.application.AttentionCategoryService;
import com.hyperlink.server.domain.attentionCategory.domain.AttentionCategoryRepository;
import com.hyperlink.server.domain.attentionCategory.domain.entity.AttentionCategory;
import com.hyperlink.server.domain.attentionCategory.dto.AttentionCategoryResponse;
import com.hyperlink.server.domain.category.domain.CategoryRepository;
import com.hyperlink.server.domain.category.domain.entity.Category;
import com.hyperlink.server.domain.category.exception.CategoryNotFoundException;
import com.hyperlink.server.domain.member.domain.Career;
import com.hyperlink.server.domain.member.domain.CareerYear;
import com.hyperlink.server.domain.member.domain.MemberRepository;
import com.hyperlink.server.domain.member.domain.entity.Member;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest
class AttentionCategoryServiceIntegrationTest {

  @Autowired
  private AttentionCategoryService attentionCategoryService;
  @Autowired
  AttentionCategoryRepository attentionCategoryRepository;
  @Autowired
  private MemberRepository memberRepository;
  @Autowired
  private CategoryRepository categoryRepository;

  @DisplayName("관심목록을 추가할 수 있다.")
  @Test
  void setAttentionCategoryCorrectTest() {
    Category develop = categoryRepository.save(new Category("develop"));
    Category beauty = categoryRepository.save(new Category("beauty"));
    List<String> attentionCategories = Arrays.asList("develop", "beauty");
    Member savedMember = memberRepository.save(
        new Member("rldnd5555@gmail.com", "chocho", Career.DEVELOP, CareerYear.MORE_THAN_TEN, "url",
            1990, "man"));

    attentionCategoryService.changeAttentionCategory(savedMember.getId(), attentionCategories);
    List<AttentionCategory> allAttentionCategory = attentionCategoryRepository.findAll();

    List<Category> result = allAttentionCategory.stream()
        .filter(attentionCategory -> attentionCategory.getMember().getId() == savedMember.getId())
        .map(attentionCategory -> attentionCategory.getCategory())
        .collect(Collectors.toList());

    assertThat(result.size()).isEqualTo(2);
    assertThat(result).contains(develop);
    assertThat(result).contains(beauty);
  }

  @DisplayName("관심목록 추가시 해당하는 Category가 없다면 CategoryNotFoundException 을 던진다.")
  @Test
  void setAttentionCategoryInCorrectTest() {
    List<String> attentionCategorys = Arrays.asList("food", "beauty");
    Member savedMember = memberRepository.save(
        new Member("rldnd5555@gmail.com", "chocho", Career.DEVELOP, CareerYear.MORE_THAN_TEN, "url",
            1990, "man"));
    assertThatThrownBy(() ->
        attentionCategoryService.changeAttentionCategory(savedMember.getId(), attentionCategorys)
    ).isInstanceOf(
        CategoryNotFoundException.class);
  }

  @DisplayName("멤버의 관심 목록을 가져올 수 있다.")
  @Test
  void getAttentionCategoriesTest() {

    Category develop = categoryRepository.save(new Category("develop"));
    Category beauty = categoryRepository.save(new Category("beauty"));
    List<String> attentionCategories = Arrays.asList("develop", "beauty");
    Member savedMember = memberRepository.save(
        new Member("rldnd5555@gmail.com", "chocho", Career.DEVELOP, CareerYear.MORE_THAN_TEN, "url",
            1990, "man"));

    attentionCategoryService.changeAttentionCategory(savedMember.getId(), attentionCategories);

    AttentionCategoryResponse attentionCategoryResponse = attentionCategoryService.getAttentionCategory(
        savedMember.getId());
    List<String> categoryNameList = attentionCategoryResponse.attentionCategory();
    assertThat(categoryNameList).contains("develop");
    assertThat(categoryNameList).contains("beauty");


  }
}