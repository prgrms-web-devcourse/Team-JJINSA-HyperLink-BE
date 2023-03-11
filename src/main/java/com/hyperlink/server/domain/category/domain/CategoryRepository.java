package com.hyperlink.server.domain.category.domain;

import com.hyperlink.server.domain.admin.dto.CountingViewByCategoryDto;
import com.hyperlink.server.domain.category.domain.entity.Category;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CategoryRepository extends JpaRepository<Category, Long> {
  Optional<Category> findByName(String name);

  @Query("select c.id from Category c")
  List<Long> findAllCategoryIds();

  @Override
  List<Category> findAll();

  @Query(value =
      "select category.category_id as categoryId, category.name as categoryName, count(viewed_category.category_id) as viewCount from category "
          + "left join (select c.category_id, mh.created_at from member_history mh "
          + "inner join content c "
          + "on mh.content_id = c.content_id "
          + "where DATE(mh.created_at) = :date) as viewed_category "
          + "on category.category_id = viewed_category.category_id "
          + "group by category.category_id "
          + "order by category.category_id", nativeQuery = true)
  List<CountingViewByCategoryDto> countViewsByCategoryAndDate(String date);
}
