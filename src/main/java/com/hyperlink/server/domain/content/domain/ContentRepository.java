package com.hyperlink.server.domain.content.domain;

import com.hyperlink.server.domain.content.domain.entity.Content;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import javax.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ContentRepository extends JpaRepository<Content, Long> {

  @Modifying(clearAutomatically = true)
  @Query("update Content c set c.viewCount = c.viewCount + 1 where c.id = :content_id")
  void updateViewCount(@Param("content_id") Long contentId);

  List<Content> findAllByCreatorName(String creatorName);

  boolean existsByLink(String link);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select c from Content c where c.id = :contentId ")
  Optional<Content> selectForUpdate(@Param("contentId") Long contentId);

  Integer countByCreatedAtAfter(LocalDateTime date);

  @Query("select c from Content c where c.isViewable = false order by c.createdAt desc")
  Page<Content> findInactivatedContents(Pageable pageable);

  Content findByTitle(String title);

  @Query(value = "select count(content_id) from content where DATE(content.created_at) = :date", nativeQuery = true)
  int countByDate(@Param("date") String date);
}
