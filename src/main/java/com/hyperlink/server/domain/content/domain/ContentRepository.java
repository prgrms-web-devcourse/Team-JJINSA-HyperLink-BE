package com.hyperlink.server.domain.content.domain;

import com.hyperlink.server.domain.content.domain.entity.Content;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContentRepository extends JpaRepository<Content, Long> {

}
