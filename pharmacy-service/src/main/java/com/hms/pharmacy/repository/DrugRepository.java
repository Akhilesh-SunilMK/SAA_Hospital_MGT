package com.hms.pharmacy.repository;

import com.hms.pharmacy.entity.Drug;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface DrugRepository extends JpaRepository<Drug, Long> {

    Optional<Drug> findByCode(String code);

    @Query("select d from Drug d where lower(d.genericName) like lower(concat('%', :search, '%')) " +
            "or lower(d.brandName) like lower(concat('%', :search, '%')) or lower(d.code) like lower(concat('%', :search, '%'))")
    Page<Drug> search(@Param("search") String search, Pageable pageable);
}
