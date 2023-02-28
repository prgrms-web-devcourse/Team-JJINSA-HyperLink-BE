package com.hyperlink.server.creator.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hyperlink.server.domain.category.domain.CategoryRepository;
import com.hyperlink.server.domain.category.domain.entity.Category;
import com.hyperlink.server.domain.category.exception.CategoryNotFoundException;
import com.hyperlink.server.domain.creator.application.CreatorService;
import com.hyperlink.server.domain.creator.domain.CreatorRepository;
import com.hyperlink.server.domain.creator.domain.entity.Creator;
import com.hyperlink.server.domain.creator.dto.CreatorEnrollRequest;
import com.hyperlink.server.domain.creator.dto.CreatorEnrollResponse;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@DisplayName("CreatorService 통합 테스트")
public class CreatorServiceIntegrationTest {

  @Autowired
  CreatorService creatorService;

  @Autowired
  CategoryRepository categoryRepository;

  @Autowired
  CreatorRepository creatorRepository;

  @Nested
  @DisplayName("크리에이터 생성 메서드는")
  class CreatorEnrollTest {

    @Test
    @DisplayName("성공하면 크리에이터로 등록된다.")
    public void success() throws Exception {
      Category develop = new Category("develop");
      CreatorEnrollRequest creatorEnrollRequest = new CreatorEnrollRequest("크리에이터 이름",
          "profileImgUrl", "크리에이터입니다.", "develop");

      categoryRepository.save(develop);
      CreatorEnrollResponse creatorEnrollResponse = creatorService.enrollCreator(creatorEnrollRequest);
      //then
      Optional<Creator> foundCreator = creatorRepository.findById(creatorEnrollResponse.id());
      assertThat(foundCreator).isPresent();
      assertThat(foundCreator.get().getName()).isEqualTo(creatorEnrollRequest.name());
    }

    @Test
    @DisplayName("없는 카테고리 이름으로 등록하면 CategoryNotFoundException이 발생한다.")
    public void fail() throws Exception {
      CreatorEnrollRequest creatorEnrollRequest = new CreatorEnrollRequest("크리에이터 이름",
          "profileImgUrl", "크리에이터입니다.", "develop");

      assertThatThrownBy(() -> creatorService.enrollCreator(creatorEnrollRequest))
          .isInstanceOf(CategoryNotFoundException.class);
    }
  }


}
