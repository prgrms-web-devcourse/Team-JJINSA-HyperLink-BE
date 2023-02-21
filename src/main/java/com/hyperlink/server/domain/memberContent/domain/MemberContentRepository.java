package com.hyperlink.server.domain.memberContent.domain;

import com.hyperlink.server.domain.memberContent.domain.entity.MemberContent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberContentRepository extends JpaRepository<MemberContent, Long> {

}
