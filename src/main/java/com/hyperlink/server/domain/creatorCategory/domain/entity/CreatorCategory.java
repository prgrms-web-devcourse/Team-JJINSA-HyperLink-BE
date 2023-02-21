package com.hyperlink.server.domain.creatorCategory.domain.entity;

import com.hyperlink.server.domain.category.domain.entity.Category;
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

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CreatorCategory {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "creator_category_id")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "creator_id")
  private Creator creator;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "category_id")
  private Category category;

}
