package com.hms.billing.factory;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaymentProcessorFactoryTest {

    private final PaymentProcessorFactory factory = new PaymentProcessorFactory(List.of(
            new CashPaymentProcessor(),
            new CardPaymentProcessor(),
            new UpiPaymentProcessor(),
            new InsurancePaymentProcessor(),
            new NetBankingPaymentProcessor()
    ));

    @Test
    void tcF04_upiMethodReturnsUpiPaymentProcessor() {
        PaymentProcessor processor = factory.getProcessor(PaymentMethod.UPI);
        assertThat(processor).isInstanceOf(UpiPaymentProcessor.class);
    }

    @Test
    void cashMethodReturnsCashProcessor() {
        assertThat(factory.getProcessor(PaymentMethod.CASH)).isInstanceOf(CashPaymentProcessor.class);
    }

    @Test
    void unregisteredMethodThrows() {
        PaymentProcessorFactory partial = new PaymentProcessorFactory(List.of(new CashPaymentProcessor()));
        assertThatThrownBy(() -> partial.getProcessor(PaymentMethod.UPI))
                .isInstanceOf(UnsupportedPaymentMethodException.class);
    }

    @Test
    void upiProcessorValidatesVpaFormat() {
        PaymentProcessor upi = factory.getProcessor(PaymentMethod.UPI);
        assertThatThrownBy(() -> upi.process(new PaymentRequest(1L, PaymentMethod.UPI,
                java.math.BigDecimal.TEN, "not-a-vpa")))
                .isInstanceOf(com.hms.common.exception.BusinessRuleException.class);

        PaymentResult result = upi.process(new PaymentRequest(1L, PaymentMethod.UPI,
                java.math.BigDecimal.TEN, "patient@upi"));
        assertThat(result.success()).isTrue();
    }
}
