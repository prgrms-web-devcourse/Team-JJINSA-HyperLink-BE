package com.hyperlink.server.domain.category.domain;

import com.hyperlink.server.domain.category.domain.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {

}
