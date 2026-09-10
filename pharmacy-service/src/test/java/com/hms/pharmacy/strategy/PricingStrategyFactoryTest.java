package com.hms.pharmacy.strategy;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PricingStrategyFactoryTest {

    private final PricingStrategyFactory factory = new PricingStrategyFactory(List.of(
            new RetailPricingStrategy(),
            new InsuredPricingStrategy(),
            new SeniorCitizenPricingStrategy(),
            new StaffConcessionPricingStrategy()
    ));

    @Test
    void resolvesRetailStrategyWithNoDiscount() {
        PricingStrategy strategy = factory.resolve(PatientCategory.GENERAL);
        assertThat(strategy).isInstanceOf(RetailPricingStrategy.class);
        assertThat(strategy.price(BigDecimal.TEN, 2)).isEqualByComparingTo("20.00");
    }

    @Test
    void resolvesInsuredStrategyWithDiscount() {
        PricingStrategy strategy = factory.resolve(PatientCategory.INSURED);
        assertThat(strategy).isInstanceOf(InsuredPricingStrategy.class);
        assertThat(strategy.price(BigDecimal.TEN, 10)).isEqualByComparingTo("80.00");
    }

    @Test
    void addingNewStrategyRequiresNoRegistryChange() {
        PricingStrategyFactory extended = new PricingStrategyFactory(List.of(
                new RetailPricingStrategy(),
                new StaffConcessionPricingStrategy()
        ));
        assertThat(extended.resolve(PatientCategory.STAFF_CONCESSION))
                .isInstanceOf(StaffConcessionPricingStrategy.class);
    }

    @Test
    void unknownCategoryThrows() {
        PricingStrategyFactory empty = new PricingStrategyFactory(List.of(new RetailPricingStrategy()));
        assertThatThrownBy(() -> empty.resolve(PatientCategory.INSURED))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
