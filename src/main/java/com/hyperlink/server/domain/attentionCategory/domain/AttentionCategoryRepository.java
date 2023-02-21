package com.hyperlink.server.domain.attentionCategory.domain;

import com.hyperlink.server.domain.attentionCategory.domain.entity.AttentionCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttentionCategoryRepository extends JpaRepository<AttentionCategory, Long> {

}
