package com.hyperlink.server.creator.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.hyperlink.server.domain.category.domain.CategoryRepository;
import com.hyperlink.server.domain.category.domain.entity.Category;
import com.hyperlink.server.domain.creator.domain.CreatorRepository;
import com.hyperlink.server.domain.creator.domain.entity.Creator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.EmptyResultDataAccessException;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
public class CreatorRepositoryTest {

  @Autowired
  CategoryRepository categoryRepository;

  @Autowired
  CreatorRepository creatorRepository;

  @Test
  @DisplayName("컨텐트를 이름으로 검색할 수 있다.")
  void findContentByNameTest() {
    Category category = new Category("개발");
    Creator creator = new Creator("name", "profile", "description", category);
    categoryRepository.save(category);
    creatorRepository.save(creator);

    Creator developCreator = creatorRepository.findByName("name").get();
    assertThat(developCreator.getName()).isEqualTo("name");
  }

  @Nested
  @DisplayName("크리에이터 삭제시")
  class CreatorDeleteTest {

    @Test
    @DisplayName("존재하지 않는 크리에이터를 입력하면 EmptyResultDataAccessException 을 발생한다.")
    void deleteCreatorByIdTestEmptyResultDataAccessException() {
      assertThrows(EmptyResultDataAccessException.class, () -> creatorRepository.deleteById(44444444L));
    }

    @Test
    @DisplayName("존재하는 크리에이터를 입력하면 성공한다.")
    void deleteCreatorByIdTestSuccess() {
      Category category = new Category("개발");
      Creator creator = new Creator("name", "profile", "description", category);
      categoryRepository.save(category);
      creatorRepository.save(creator);

      creatorRepository.deleteById(creator.getId());

      assertThat(creatorRepository.existsById(creator.getId())).isFalse();
    }
  }
}
