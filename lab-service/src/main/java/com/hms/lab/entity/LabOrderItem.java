package com.hms.lab.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "lab_order_items")
public class LabOrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private LabOrder order;

    @Column(name = "test_id", nullable = false)
    private Long testId;

    @Column(nullable = false, length = 20)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Enumerated(EnumType.STRING)
    private LabOrderStatus status;

    protected LabOrderItem() {
        // JPA only
    }

    public LabOrderItem(Long testId) {
        this.testId = testId;
        this.status = LabOrderStatus.ORDERED;
    }

    void assignTo(LabOrder order) {
        this.order = order;
    }

    public void markStatus(LabOrderStatus status) {
        this.status = status;
    }

    public Long getId() { return id; }
    public LabOrder getOrder() { return order; }
    public Long getTestId() { return testId; }
    public LabOrderStatus getStatus() { return status; }
}
