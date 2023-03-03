package com.hyperlink.server.domain.creator.domain;

import com.hyperlink.server.domain.creator.domain.entity.Creator;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CreatorRepository extends JpaRepository<Creator, Long> {
  Optional<Creator> findByName(String name);
}
