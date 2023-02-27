package com.hyperlink.server.domain.content.domain.entity;

import com.hyperlink.server.domain.category.domain.entity.Category;
import com.hyperlink.server.domain.common.BaseEntity;
import com.hyperlink.server.domain.creator.domain.entity.Creator;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Content extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "content_id")
  private Long id;

  @Column(nullable = false)
  private String title;

  @Column(nullable = false)
  private String contentImgUrl;

  @Column(nullable = false)
  private String link;

  @JoinColumn(name = "creator_id", nullable = false)
  @ManyToOne(fetch = FetchType.LAZY)
  private Creator creator;

  @JoinColumn(name = "category_id", nullable = false)
  @ManyToOne(fetch = FetchType.LAZY)
  private Category category;

  @Column(nullable = false, name = "is_viewable", columnDefinition = "TINYINT", length = 1)
  private boolean isViewable;

  @Column(columnDefinition = "INT UNSIGNED", nullable = false)
  @ColumnDefault("0")
  private int viewCount = 0;

  @Column(columnDefinition = "INT UNSIGNED", nullable = false)
  @ColumnDefault("0")
  private int likeCount = 0;

  public Content(String title, String contentImgUrl, String link) {
    this.title = title;
    this.contentImgUrl = contentImgUrl;
    this.link = link;
  }
}
