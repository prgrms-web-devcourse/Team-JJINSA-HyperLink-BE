package com.hyperlink.server.domain.content.domain;

import com.hyperlink.server.domain.content.domain.entity.Content;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ContentRepository extends JpaRepository<Content, Long> {

  @Modifying(clearAutomatically = true)
  @Query("update Content c set c.viewCount = c.viewCount + 1 where c.id = :content_id")
  int updateInquiryCount(@Param("content_id") Long contentId);

}
