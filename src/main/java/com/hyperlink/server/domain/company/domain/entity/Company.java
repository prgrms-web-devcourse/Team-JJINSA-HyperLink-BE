package com.hyperlink.server.domain.company.domain.entity;

import com.hyperlink.server.domain.common.BaseEntity;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

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

  @Column(nullable = false, name = "is_using_recommend", columnDefinition = "TINYINT", length = 1)
  @ColumnDefault("0")
  private Boolean isUsingRecommend;

  public Company(String emailAddress, String name) {
    this.emailAddress = emailAddress;
    this.name = name;
    this.logoImgUrl = "https://hyperlink-data.s3.ap-northeast-2.amazonaws.com/company_logo_image/logo_default.png";
    this.isUsingRecommend = false;
  }

  public Company(String emailAddress, String logoImgUrl, String name) {
    this.emailAddress = emailAddress;
    this.logoImgUrl = logoImgUrl;
    this.name = name;
    this.isUsingRecommend = false;
  }

  public void changeIsUsingRecommend(Boolean isUsingRecommend) {
    this.isUsingRecommend = isUsingRecommend;
  }

  public void changeCompanyName(String companyName) {
    this.name = companyName;
  }
}
