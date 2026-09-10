package com.hms.lab.service;

import com.hms.lab.entity.TestCatalogue;
import com.hms.lab.repository.TestCatalogueRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TestCatalogueService {

    private final TestCatalogueRepository testCatalogueRepository;

    public TestCatalogueService(TestCatalogueRepository testCatalogueRepository) {
        this.testCatalogueRepository = testCatalogueRepository;
    }

    @Transactional(readOnly = true)
    public List<TestCatalogue> listAll() {
        return testCatalogueRepository.findAll();
    }
}
