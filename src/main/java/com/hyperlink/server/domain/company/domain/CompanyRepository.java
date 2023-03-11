package com.hyperlink.server.domain.company.domain;

import com.hyperlink.server.domain.company.domain.entity.Company;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyRepository extends JpaRepository<Company, Long> {

  Page<Company> findCompaniesByIsUsingRecommend(Boolean isUsingRecommend, Pageable pageable);

  Optional<Company> findByEmailAddress(String emailAddress);

  Boolean existsByEmailAddress(String emailAddress);
}
