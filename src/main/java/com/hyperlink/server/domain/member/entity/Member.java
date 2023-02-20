package com.hyperlink.server.domain.member.entity;

import com.hyperlink.server.domain.common.BaseEntity;
import com.hyperlink.server.domain.company.entity.Company;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.validation.constraints.Email;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
  private String career;

  @Column(length = 30, nullable = false)
  private String careerYear;

  @Column(nullable = false)
  private String profileImg;

  @Column(columnDefinition = "INT UNSIGNED")
  private Integer birth_year;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "company_id")
  private Company company;

}
