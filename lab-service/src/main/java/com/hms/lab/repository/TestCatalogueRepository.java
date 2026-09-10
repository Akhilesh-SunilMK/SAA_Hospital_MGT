package com.hms.lab.repository;

import com.hms.lab.entity.TestCatalogue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TestCatalogueRepository extends JpaRepository<TestCatalogue, Long> {
    Optional<TestCatalogue> findByCode(String code);
}
