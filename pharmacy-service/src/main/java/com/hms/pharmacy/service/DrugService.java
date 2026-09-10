package com.hms.pharmacy.service;

import com.hms.common.exception.ConflictException;
import com.hms.common.exception.ResourceNotFoundException;
import com.hms.pharmacy.dto.DrugRequest;
import com.hms.pharmacy.dto.DrugResponse;
import com.hms.pharmacy.dto.LowStockResponse;
import com.hms.pharmacy.dto.StockAdjustRequest;
import com.hms.pharmacy.entity.Drug;
import com.hms.pharmacy.entity.StockBatch;
import com.hms.pharmacy.repository.DrugRepository;
import com.hms.pharmacy.repository.StockBatchRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DrugService {

    private final DrugRepository drugRepository;
    private final StockBatchRepository stockBatchRepository;

    public DrugService(DrugRepository drugRepository, StockBatchRepository stockBatchRepository) {
        this.drugRepository = drugRepository;
        this.stockBatchRepository = stockBatchRepository;
    }

    @Transactional
    public DrugResponse create(DrugRequest req) {
        drugRepository.findByCode(req.code()).ifPresent(d -> {
            throw new ConflictException("A drug with code " + req.code() + " already exists");
        });
        Drug drug = new Drug(req.code(), req.genericName(), req.brandName(), req.form(), req.strength(),
                req.manufacturer(), req.unitPrice(), req.reorderLevel());
        try {
            return DrugResponse.from(drugRepository.save(drug));
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("A drug with code " + req.code() + " already exists");
        }
    }

    public Page<DrugResponse> search(String search, Pageable pageable) {
        Page<Drug> page = (search == null || search.isBlank())
                ? drugRepository.findAll(pageable)
                : drugRepository.search(search, pageable);
        return page.map(DrugResponse::from);
    }

    @Transactional
    public void adjustStock(Long drugId, StockAdjustRequest req) {
        Drug drug = drugRepository.findById(drugId)
                .orElseThrow(() -> new ResourceNotFoundException("Drug not found: " + drugId));
        StockBatch batch = new StockBatch(drug.getId(), req.batchNo(), req.quantity(), req.expiryDate());
        stockBatchRepository.save(batch);
    }

    public List<LowStockResponse> lowStock() {
        return drugRepository.findAll().stream()
                .map(drug -> {
                    int total = stockBatchRepository.findByDrugId(drug.getId()).stream()
                            .filter(b -> !b.isExpired())
                            .mapToInt(StockBatch::getQuantity)
                            .sum();
                    return new LowStockResponse(drug.getId(), drug.getCode(), drug.getGenericName(),
                            total, drug.getReorderLevel());
                })
                .filter(r -> r.totalQuantity() <= r.reorderLevel())
                .toList();
    }
}
