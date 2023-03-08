package com.hyperlink.server.domain.memberContent.domain.entity;

import com.hyperlink.server.domain.common.BaseEntity;
import com.hyperlink.server.domain.content.domain.entity.Content;
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
public class MemberContent extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "member_content_id")
  private Long id;

  @Column(columnDefinition = "INT UNSIGNED", nullable = false)
  private Integer type;

  @Column
  private Long memberId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "content_id")
  private Content content;

  public MemberContent(Long memberId, Content content, MemberContentActionType type) {
    this.memberId = memberId;
    this.content = content;
    this.type = type.getTypeNumber();
  }
}
