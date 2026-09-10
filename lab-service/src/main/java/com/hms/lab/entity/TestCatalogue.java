package com.hms.lab.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "test_catalogue")
public class TestCatalogue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String code;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(name = "sample_type", nullable = false, length = 30)
    private String sampleType;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "turnaround_hours", nullable = false)
    private Integer turnaroundHours;

    protected TestCatalogue() {
        // JPA only
    }

    public TestCatalogue(String code, String name, String sampleType, BigDecimal price, Integer turnaroundHours) {
        this.code = code;
        this.name = name;
        this.sampleType = sampleType;
        this.price = price;
        this.turnaroundHours = turnaroundHours;
    }

    public Long getId() { return id; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public String getSampleType() { return sampleType; }
    public BigDecimal getPrice() { return price; }
    public Integer getTurnaroundHours() { return turnaroundHours; }
}
