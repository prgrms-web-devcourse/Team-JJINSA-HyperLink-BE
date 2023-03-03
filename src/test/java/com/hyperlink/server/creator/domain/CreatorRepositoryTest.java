package com.hyperlink.server.creator.domain;

import com.hyperlink.server.domain.category.domain.CategoryRepository;
import com.hyperlink.server.domain.category.domain.entity.Category;
import com.hyperlink.server.domain.content.domain.entity.Content;
import com.hyperlink.server.domain.creator.domain.CreatorRepository;
import com.hyperlink.server.domain.creator.domain.entity.Creator;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

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
    Assertions.assertThat(developCreator.getName()).isEqualTo("name");
  }

}
