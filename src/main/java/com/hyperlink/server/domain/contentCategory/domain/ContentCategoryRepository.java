package com.hyperlink.server.domain.contentCategory.domain;

import com.hyperlink.server.domain.contentCategory.domain.entity.ContentCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContentCategoryRepository extends JpaRepository<ContentCategory, Long> {

}
