package com.hyperlink.server.domain.creator.domain;

import com.hyperlink.server.domain.creator.domain.entity.Creator;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CreatorRepository extends JpaRepository<Creator, Long> {
  Optional<Creator> findByName(String name);

  @Query(value = "select c from Creator c join fetch c.category",
  countQuery = "select count(c) from Creator c")
  Page<Creator> findCreators(Pageable pageable);
}
