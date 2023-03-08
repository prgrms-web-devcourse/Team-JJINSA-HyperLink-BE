package com.hyperlink.server.domain.member.domain.entity;

import com.hyperlink.server.domain.common.BaseEntity;
import com.hyperlink.server.domain.company.domain.entity.Company;
import com.hyperlink.server.domain.member.domain.Career;
import com.hyperlink.server.domain.member.domain.CareerYear;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
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
public class Member extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "member_id")
  private Long id;

  @Column(nullable = false, unique = true)
  private String email;

  @Column(length = 30, nullable = false)
  private String nickname;

  @Column(length = 30, nullable = false)
  @Enumerated(EnumType.STRING)
  private Career career;

  @Column(length = 30, nullable = false)
  @Enumerated(EnumType.STRING)
  private CareerYear careerYear;

  @Column(nullable = false)
  private String profileImgUrl;

  @Column(columnDefinition = "INT UNSIGNED")
  private Integer birthYear;

  @Column(length = 10)
  private String gender;

  @Column(nullable = false, name = "is_admin", columnDefinition = "TINYINT", length = 1)
  @ColumnDefault("0")
  private Boolean isAdmin;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "company_id")
  private Company company;

  public Member(String email, String nickname, Career career, CareerYear careerYear,
      String profileImgUrl) {
    this.email = email;
    this.nickname = nickname;
    this.career = career;
    this.careerYear = careerYear;
    this.profileImgUrl = profileImgUrl;
  }

  public Member(String email, String nickname, Career career, CareerYear careerYear,
      String profileImgUrl,
      Integer birthYear, String gender) {
    this.email = email;
    this.nickname = nickname;
    this.career = career;
    this.careerYear = careerYear;
    this.profileImgUrl = profileImgUrl;
    this.birthYear = birthYear;
    this.gender = gender;
    this.isAdmin = false;
  }

  public Member changeMember(String nickname, Career career, CareerYear careerYear) {
    this.nickname = nickname;
    this.career = career;
    this.careerYear = careerYear;

    return this;
  }

  public void changeProfileImgUrl(String profileImgUrl) {
    this.profileImgUrl = profileImgUrl;
  }

  public void changeCompany(Company company) {
    this.company = company;
  }

}
