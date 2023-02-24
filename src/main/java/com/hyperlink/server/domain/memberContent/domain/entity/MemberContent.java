package com.hyperlink.server.domain.memberContent.domain.entity;

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
public class MemberContent extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "member_content_id")
  private Long id;

  @Column(columnDefinition = "INT UNSIGNED", nullable = false)
  private Integer type;

  @Column
  private Long memberId;

  @Column
  private Long contentId;


  public MemberContent(Long memberId, Long contentId, MemberContentActionType type) {
    this.memberId = memberId;
    this.contentId = contentId;
    this.type = type.getTypeNumber();
  }
}