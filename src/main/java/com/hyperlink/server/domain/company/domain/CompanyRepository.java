package com.hyperlink.server.domain.company.domain;

import com.hyperlink.server.domain.company.domain.entity.Company;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyRepository extends JpaRepository<Company, Long> {

  Optional<Company> findByEmailAddress(String emailAddress);
}
