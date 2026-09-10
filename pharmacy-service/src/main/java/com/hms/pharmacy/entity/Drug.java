package com.hms.pharmacy.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "drugs")
@Getter
@NoArgsConstructor
public class Drug {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @Column(name = "generic_name", nullable = false, length = 150)
    private String genericName;

    @Column(name = "brand_name", length = 150)
    private String brandName;

    @Column(nullable = false, length = 30)
    private String form;

    @Column(nullable = false, length = 30)
    private String strength;

    @Column(length = 150)
    private String manufacturer;

    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "reorder_level", nullable = false)
    private Integer reorderLevel = 10;

    public Drug(String code, String genericName, String brandName, String form, String strength,
                String manufacturer, BigDecimal unitPrice, Integer reorderLevel) {
        this.code = code;
        this.genericName = genericName;
        this.brandName = brandName;
        this.form = form;
        this.strength = strength;
        this.manufacturer = manufacturer;
        this.unitPrice = unitPrice;
        this.reorderLevel = reorderLevel != null ? reorderLevel : 10;
    }
}
