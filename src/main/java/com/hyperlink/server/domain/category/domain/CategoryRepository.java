package com.hyperlink.server.domain.category.domain;

import com.hyperlink.server.domain.category.domain.entity.Category;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CategoryRepository extends JpaRepository<Category, Long> {
  Optional<Category> findByName(String name);

  @Query("select c.id from Category c")
  List<Long> findAllCategoryIds();
}
