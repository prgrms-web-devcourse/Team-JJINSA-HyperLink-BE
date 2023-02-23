package com.hyperlink.server.memberContent.repository;

import com.hyperlink.server.domain.memberContent.domain.MemberContentRepository;
import com.hyperlink.server.domain.memberContent.domain.entity.MemberContent;
import com.hyperlink.server.domain.memberContent.domain.entity.MemberContentActionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
public class MemberContentRespositoryTest {

  @Autowired
  MemberContentRepository memberContentRepository;

//  @Test
//  @DisplayName("findMemberContentByMemberIdAndContentIdAndType 메소드")
//  void findMemberContentByMemberIdAndContentIdAndTypeTest() {
//  }
}
