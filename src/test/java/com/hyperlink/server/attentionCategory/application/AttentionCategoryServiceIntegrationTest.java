package com.hyperlink.server.attentionCategory.application;

import com.hyperlink.server.domain.attentionCategory.application.AttentionCategoryService;
import com.hyperlink.server.domain.member.domain.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest
class AttentionCategoryServiceIntegrationTest {

  @Autowired
  private AttentionCategoryService attentionCategoryService;
  @Autowired
  private MemberRepository memberRepository;

  @DisplayName("관심목록을 추가할 수 있다.")
  @Test
  void setAttentionCategoryTest() {

  }

}