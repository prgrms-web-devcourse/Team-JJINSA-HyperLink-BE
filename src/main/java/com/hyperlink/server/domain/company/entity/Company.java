package com.hyperlink.server.domain.company.entity;

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
public class Company extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "company_id")
  private Long id;

  @Column(length = 50, nullable = false)
  private String emailAddress;

  @Column(nullable = false)
  private String logoImgUrl;

  @Column(length = 30, nullable = false)
  private String name;

}
