package com.hyperlink.server.category.domain;


import static org.assertj.core.api.Assertions.assertThat;

import com.hyperlink.server.domain.admin.dto.CountingViewByCategoryDto;
import com.hyperlink.server.domain.category.domain.CategoryRepository;
import com.hyperlink.server.domain.category.domain.entity.Category;
import com.hyperlink.server.domain.content.domain.ContentRepository;
import com.hyperlink.server.domain.content.domain.entity.Content;
import com.hyperlink.server.domain.creator.domain.CreatorRepository;
import com.hyperlink.server.domain.creator.domain.entity.Creator;
import com.hyperlink.server.domain.member.domain.Career;
import com.hyperlink.server.domain.member.domain.CareerYear;
import com.hyperlink.server.domain.member.domain.MemberRepository;
import com.hyperlink.server.domain.member.domain.entity.Member;
import com.hyperlink.server.domain.memberHistory.domain.MemberHistoryRepository;
import com.hyperlink.server.domain.memberHistory.domain.entity.MemberHistory;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
public class CategoryRepositoryTest {

  @Autowired
  CategoryRepository categoryRepository;
  @Autowired
  ContentRepository contentRepository;
  @Autowired
  CreatorRepository creatorRepository;
  @Autowired
  MemberRepository memberRepository;
  @Autowired
  MemberHistoryRepository memberHistoryRepository;

  @Test
  @DisplayName("카테고리를 이름으로 검색할 수 있다.")
  void find_category_by_name() {
    Category beauty = new Category("beauty");
    categoryRepository.save(beauty);

    Optional<Category> categoryOptional = categoryRepository.findByName(beauty.getName());
    assertThat(categoryOptional).isPresent();
    assertThat(categoryOptional.get().getName()).isEqualTo(beauty.getName());
  }

  @Test
  @DisplayName("원하는 날짜의 카테고리 별 조회수를 조회할 수 있다.")
  void countViewCountByCategory() throws InterruptedException {
    LocalDateTime now = LocalDateTime.now();
    if(now.getHour() == 23 && now.getMinute() == 59) {
      Thread.sleep(60000);
    }
    LocalDate date = LocalDate.now();
    Member member = new Member("email", "nickname", Career.DEVELOP, CareerYear.EIGHT,
        "profileImgUrl");
    memberRepository.save(member);
    Category categoryDevelop = new Category("develop");
    categoryRepository.save(categoryDevelop);
    Category categoryBeauty = new Category("beauty");
    categoryRepository.save(categoryBeauty);
    Category categoryFinance = new Category("finance");
    categoryRepository.save(categoryFinance);

    Creator creatorDevelop = new Creator("develop크리에이터", "profileImgUrl", "description", categoryDevelop);
    Creator creatorBeauty = new Creator("beauty크리에이터", "profileImgUrl", "description", categoryBeauty);
    creatorRepository.save(creatorDevelop);
    creatorRepository.save(creatorBeauty);
    Content content1 = contentRepository.save(
        new Content("제목1", "contentImgUrl", "link1", creatorDevelop, categoryDevelop));
    Content content2 = contentRepository.save(
        new Content("제목2", "contentImgUrl", "link1", creatorBeauty, categoryBeauty));
    Content content3 = contentRepository.save(
        new Content("제목3", "contentImgUrl", "link1", creatorBeauty, categoryBeauty));

    memberHistoryRepository.save(new MemberHistory(member, content1, false));
    memberHistoryRepository.save(new MemberHistory(member, content2, false));
    memberHistoryRepository.save(new MemberHistory(member, content3, false));

    List<CountingViewByCategoryDto> countingViewByCategoryDtos = categoryRepository.countViewsByCategoryAndDate(
        date.toString());

    assertThat(countingViewByCategoryDtos).hasSize(3);
    assertThat(countingViewByCategoryDtos.get(0).getCategoryName()).isEqualTo("develop");
    assertThat(countingViewByCategoryDtos.get(0).getViewCount()).isEqualTo(1);
    assertThat(countingViewByCategoryDtos.get(1).getCategoryName()).isEqualTo("beauty");
    assertThat(countingViewByCategoryDtos.get(1).getViewCount()).isEqualTo(2);
    assertThat(countingViewByCategoryDtos.get(2).getCategoryName()).isEqualTo("finance");
    assertThat(countingViewByCategoryDtos.get(2).getViewCount()).isEqualTo(0);
  }
}
