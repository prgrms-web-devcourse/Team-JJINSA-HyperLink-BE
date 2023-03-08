package com.hyperlink.server.dailyBriefing.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import com.hyperlink.server.domain.attentionCategory.domain.AttentionCategoryRepository;
import com.hyperlink.server.domain.attentionCategory.domain.entity.AttentionCategory;
import com.hyperlink.server.domain.category.domain.CategoryRepository;
import com.hyperlink.server.domain.category.domain.entity.Category;
import com.hyperlink.server.domain.category.domain.vo.CategoryType;
import com.hyperlink.server.domain.creator.domain.CreatorRepository;
import com.hyperlink.server.domain.dailyBriefing.infrastructure.DailyBriefingRepositoryCustom;
import com.hyperlink.server.domain.member.domain.Career;
import com.hyperlink.server.domain.member.domain.CareerYear;
import com.hyperlink.server.domain.member.domain.MemberRepository;
import com.hyperlink.server.domain.member.domain.entity.Member;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
public class DailyBriefingRepositoryCustomTest {

  @Autowired
  DailyBriefingRepositoryCustom dailyBriefingRepositoryCustom;
  @Autowired
  AttentionCategoryRepository attentionCategoryRepository;
  @Autowired
  MemberRepository memberRepository;
  @Autowired
  CreatorRepository creatorRepository;
  @Autowired
  CategoryRepository categoryRepository;

  @Nested
  @DisplayName("관심카테고리별 회원 숫자를 조회해오는 메서드는")
  class GetMemberCountByAttentionCategoriesTest{

    List<Category> categories = new ArrayList<>();

    @Test
    @DisplayName("attentionCategory 테이블에서 categoryId 조건에 맞는 행을 count 한다")
    void getMemberCountByAttentionCategories() {
      Member member = new Member("email", "nickname", Career.DEVELOP, CareerYear.MORE_THAN_TEN,
          "profileImgUrl");
      memberRepository.save(member);

      for (CategoryType categoryType : CategoryType.values()) {
        Category category = new Category(categoryType.getRequestParamName());
        categories.add(category);
      }
      categoryRepository.saveAll(categories);

      Category category0 = categories.get(0);
      final long memberCount = 4L;
      for (int i = 0; i < memberCount; i++) {
        AttentionCategory attentionCategory = new AttentionCategory(member, category0);
        attentionCategoryRepository.save(attentionCategory);
      }

      Category category1 = categories.get(1);
      AttentionCategory attentionCategory = new AttentionCategory(member, category1);
      attentionCategoryRepository.save(attentionCategory);

      Category category2 = categories.get(2);

      Optional<Long> memberCountByCategory0 = dailyBriefingRepositoryCustom.getMemberCountByAttentionCategories(
          category0.getId());
      Optional<Long> memberCountByCategory1 = dailyBriefingRepositoryCustom.getMemberCountByAttentionCategories(
          category1.getId());
      Optional<Long> memberCountByCategory2 = dailyBriefingRepositoryCustom.getMemberCountByAttentionCategories(
          category2.getId());

      assertThat(memberCountByCategory0).contains(memberCount);
      assertThat(memberCountByCategory1.get()).isOne();
      assertThat(memberCountByCategory2.get()).isZero();
    }
  }
}
