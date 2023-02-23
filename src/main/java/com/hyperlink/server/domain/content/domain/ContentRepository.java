package com.hyperlink.server.domain.content.domain;

import com.hyperlink.server.domain.content.domain.entity.Content;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface ContentRepository extends JpaRepository<Content, Long> {

  @Modifying(clearAutomatically = true)
  @Query("update Content c set c.inquiry = c.inquiry + 1")
  int updateInquiryCount(Long contentId);

}
