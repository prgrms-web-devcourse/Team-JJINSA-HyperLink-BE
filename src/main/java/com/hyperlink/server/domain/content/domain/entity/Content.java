package com.hyperlink.server.domain.content.domain.entity;

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

  public Content(String title, String contentImgUrl, String link) {
    this.title = title;
    this.contentImgUrl = contentImgUrl;
    this.link = link;
  }
}