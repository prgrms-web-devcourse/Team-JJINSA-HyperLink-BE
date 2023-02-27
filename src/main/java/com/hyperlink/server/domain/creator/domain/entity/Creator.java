package com.hyperlink.server.domain.creator.domain.entity;

import com.hyperlink.server.domain.category.domain.entity.Category;
import com.hyperlink.server.domain.common.BaseEntity;
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
public class Creator extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "creator_id")
  private Long id;

  @Column(nullable = false, unique = true)
  private String name;

  @Column(nullable = false)
  private String profileImgUrl;

  @Column(nullable = false)
  private String description;
 
  @JoinColumn(name = "category_id", nullable = false)
  @ManyToOne(fetch = FetchType.LAZY)
  private Category category;


  public String getCategoryName() {
    return category.getName();
  }

  public Creator(String name, String profileImgUrl, String description, Category category) {
    this.name = name;
    this.profileImgUrl = profileImgUrl;
    this.description = description;
    this.category = category;
  }

  @JoinColumn(name = "category_id", nullable = false)
  @ManyToOne(fetch = FetchType.LAZY)
  private Category category;

  public String getCategoryName() {
    return category.getName();
  }

  public Creator(String name, String profileImgUrl, String description, Category category) {
    this.name = name;
    this.profileImgUrl = profileImgUrl;
    this.description = description;
    this.category = category;
  }
}
