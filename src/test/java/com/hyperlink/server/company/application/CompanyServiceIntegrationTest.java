package com.hyperlink.server.company.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.hyperlink.server.domain.category.domain.CategoryRepository;
import com.hyperlink.server.domain.category.domain.entity.Category;
import com.hyperlink.server.domain.company.application.CompanyService;
import com.hyperlink.server.domain.company.domain.CompanyRepository;
import com.hyperlink.server.domain.company.domain.entity.Company;
import com.hyperlink.server.domain.company.dto.CompanyChangeNameRequest;
import com.hyperlink.server.domain.company.dto.CompanyPageResponse;
import com.hyperlink.server.domain.company.dto.CompanyRegisterRequest;
import com.hyperlink.server.domain.company.dto.MailAuthVerifyRequest;
import com.hyperlink.server.domain.company.dto.MailRequest;
import com.hyperlink.server.domain.company.exception.CompanyAlreadyExistException;
import com.hyperlink.server.domain.company.exception.CompanyNotFoundException;
import com.hyperlink.server.domain.company.exception.MailAuthInvalidException;
import com.hyperlink.server.domain.company.mail.MailAuth;
import com.hyperlink.server.domain.company.mail.MailAuthRepository;
import com.hyperlink.server.domain.member.domain.Career;
import com.hyperlink.server.domain.member.domain.CareerYear;
import com.hyperlink.server.domain.member.domain.MemberRepository;
import com.hyperlink.server.domain.member.domain.entity.Member;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Transactional
@SpringBootTest
class CompanyServiceIntegrationTest {

  @Autowired
  private CompanyService companyService;

  @Autowired
  private CompanyRepository companyRepository;

  @Autowired
  private CategoryRepository categoryRepository;

  @Autowired
  private MemberRepository memberRepository;

  @Autowired
  private MailAuthRepository mailAuthRepository;

  @DisplayName("요청된 회사이메일에 대한 내용을 redis에 저장할 수 있다.")
  @Test
  void saveMailAuthNumberTest() {
    String email = "rldnd2637@naver.com";
    int authNumber = 123456;
    MailRequest mailRequest = new MailRequest(email);
    companyService.saveMailAuthNumber(mailRequest, authNumber);

    MailAuth foundMailAuth = mailAuthRepository.findById(email)
        .orElseThrow(MailAuthInvalidException::new);

    assertThat(foundMailAuth.getCompanyEmail()).isEqualTo(email);
    assertThat(foundMailAuth.getAuthNumber()).isEqualTo(authNumber);
  }

  @DisplayName("isUsingRecommend 가 false 인 회사 목록을 페이징 처리하여 가져올 수 있다.")
  @Test
  void findCompaniesForPageTest() {
    for (int i = 0; i < 6; i++) {
      companyRepository.save(
          new Company("gmail.com" + i, "gmail" + i));
    }
    CompanyPageResponse companyPageResponse1 = companyService.findCompaniesForPage(0, 2);
    CompanyPageResponse companyPageResponse2 = companyService.findCompaniesForPage(1, 2);
    CompanyPageResponse companyPageResponse3 = companyService.findCompaniesForPage(2, 2);

    assertThat(companyPageResponse1.totalPage()).isEqualTo(3);
    assertThat(companyPageResponse1.companies().size()).isEqualTo(2);
    log.info("#### company1: " + companyPageResponse1.companies().get(0));
    log.info("#### company1: " + companyPageResponse1.companies().get(1));

    assertThat(companyPageResponse2.totalPage()).isEqualTo(3);
    assertThat(companyPageResponse2.companies().size()).isEqualTo(2);
    log.info("#### company2: " + companyPageResponse2.companies().get(0));
    log.info("#### company2: " + companyPageResponse2.companies().get(1));

    assertThat(companyPageResponse3.totalPage()).isEqualTo(3);
    assertThat(companyPageResponse3.companies().size()).isEqualTo(2);
    log.info("#### company3: " + companyPageResponse3.companies().get(0));
    log.info("#### company3: " + companyPageResponse3.companies().get(1));
  }

  @DisplayName("회사 이메일 인증 완료시 해당 회사 이메일을 저장할 수 있다.")
  @Test
  void verifyAuthCompanyMailTest() {
    Category develop = categoryRepository.save(new Category("develop"));
    Category beauty = categoryRepository.save(new Category("beauty"));

    Member saveMember = memberRepository.save(
        new Member("rldnd1234@gmail.com", "Chocho", Career.DEVELOP, CareerYear.MORE_THAN_TEN,
            "localhost", 1995, "man"));

    String email = "rldnd2637@naver.com";

    int authNumber = 123456;
    companyService.saveMailAuthNumber(new MailRequest(email), authNumber);

    MailAuthVerifyRequest mailAuthVerifyRequest = new MailAuthVerifyRequest(email, authNumber);

    companyService.verifyAuthCompanyMail(saveMember.getId(), mailAuthVerifyRequest);

    Company company = saveMember.getCompany();

    assertThat(company.getEmailAddress()).isEqualTo("naver.com");
    assertThat(company.getName()).isEqualTo("naver");
    assertThat(company.getLogoImgUrl()).isEqualTo(
        "https://hyperlink-data.s3.ap-northeast-2.amazonaws.com/company_logo_image/logo_default.png");
  }

  @DisplayName("회사의 추천서비스 여부를 변경할 수 있다.")
  @Test
  void changeIsUsingRecommendTest() {
    Company savedCompany = companyRepository.save(new Company("kakao.com", "kakao"));
    boolean priorValue = savedCompany.getIsUsingRecommend();

    companyService.changeIsUsingRecommend(savedCompany.getId());
    Company foundCompany = companyRepository.findById(savedCompany.getId()).orElseThrow(
        CompanyNotFoundException::new);
    boolean changeValue = foundCompany.getIsUsingRecommend();

    Assertions.assertThat(priorValue).isFalse();
    Assertions.assertThat(changeValue).isTrue();
  }

  @DisplayName("회사를 등록할 수 있다.")
  @Test
  void registerTest() {
    CompanyRegisterRequest companyRegisterRequest = new CompanyRegisterRequest("kakao.com",
        "LogoURL", "kakao");

    companyService.createCompany(companyRegisterRequest);
    Company foundCompany = companyRepository.findByEmailAddress(
        companyRegisterRequest.emailAddress()).orElseThrow(
        CompanyNotFoundException::new);

    Assertions.assertThat(foundCompany.getEmailAddress())
        .isEqualTo(companyRegisterRequest.emailAddress());
    Assertions.assertThat(foundCompany.getLogoImgUrl())
        .isEqualTo(companyRegisterRequest.logoImgUrl());
    Assertions.assertThat(foundCompany.getName()).isEqualTo(companyRegisterRequest.companyName());
  }

  @DisplayName("이미 등록된 회사를 등록하려 한다면 CompanyAlreadyExistException을 던진다.")
  @Test
  void registerInCorrectTest() {
    CompanyRegisterRequest companyRegisterRequest = new CompanyRegisterRequest("kakao.com",
        "LogoURL", "kakao");

    companyService.createCompany(companyRegisterRequest);

    Assertions.assertThatThrownBy(() -> companyService.createCompany(companyRegisterRequest))
        .isInstanceOf(
            CompanyAlreadyExistException.class);
  }

  @DisplayName("회사 이름을 변경할 수 있다.")
  @Test
  void changeCompanyNameTest() {

    Company savedCompany = companyRepository.save(new Company("kakao.com", "kakao"));
    boolean priorValue = savedCompany.getIsUsingRecommend();

    String companyName = "newKakao";
    CompanyChangeNameRequest companyChangeNameRequest = new CompanyChangeNameRequest(companyName);

    companyService.changeCompanyName(savedCompany.getId(), companyChangeNameRequest);

    Company foundCompany = companyRepository.findById(savedCompany.getId()).orElseThrow(
        CompanyNotFoundException::new);

    Assertions.assertThat(foundCompany.getName()).isEqualTo(companyName);
  }

  @DisplayName("회사 이름을 변경시 존재하지 않는 회사라면 CompanyNotFoundException을 던진다.")
  @Test
  void changeCompanyNameInCorrectTest() {
    String companyName = "newKakao";
    CompanyChangeNameRequest companyChangeNameRequest = new CompanyChangeNameRequest(companyName);
    Assertions.assertThatThrownBy(
            () -> companyService.changeCompanyName(999L, companyChangeNameRequest))
        .isInstanceOf(CompanyNotFoundException.class);
  }

}