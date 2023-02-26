package com.hyperlink.server.domain.creator.domain.entity;

import com.hyperlink.server.domain.common.BaseEntity;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
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

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private String profileImgUrl;

  @Column(nullable = false)
  private String description;

  public Creator(String name, String profileImgUrl, String description) {
    this.name = name;
    this.profileImgUrl = profileImgUrl;
    this.description = description;
  }
}
