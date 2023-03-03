package com.hyperlink.server.category.domain;


import static org.assertj.core.api.Assertions.assertThat;

import com.hyperlink.server.domain.category.domain.CategoryRepository;
import com.hyperlink.server.domain.category.domain.entity.Category;
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

  @Test
  @DisplayName("카테고리를 이름으로 검색할 수 있다.")
  void find_category_by_name() {
//    Category developCategory = new Category("develop");
//    categoryRepository.save(developCategory);

    Optional<Category> categoryOptional = categoryRepository.findByName("develop");
    assertThat(categoryOptional).isPresent();
    assertThat(categoryOptional.get().getName()).isEqualTo("develop");
  }
}
