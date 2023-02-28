package com.hyperlink.server.category.domain;

import com.hyperlink.server.domain.category.domain.CategoryRepository;
import com.hyperlink.server.domain.category.domain.entity.Category;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CategoryRepositoryTest {

  @Autowired
  private CategoryRepository categoryRepository;

  @DisplayName("이름으로 카테고리를 가져올 수 있다.")
  @Test
  void findCategoriesByNameCorrectTest() {
    Category savedCategory = categoryRepository.save(new Category("develop"));
    Category foundCategory = categoryRepository.findByName("develop").get();
    Assertions.assertThat(foundCategory).usingRecursiveComparison().isEqualTo(savedCategory);
  }
}