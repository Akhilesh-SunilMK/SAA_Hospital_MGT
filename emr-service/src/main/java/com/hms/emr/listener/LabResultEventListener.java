package com.hms.emr.listener;

import com.hms.common.event.DomainEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * SRS 3.3: LabResultReadyEvent -> EMR Service (and Notification Service, handled separately in
 * lab/notification). A full auto-update of the clinical record from a lab result is out of
 * scope here — this listener demonstrates the async wiring and logs receipt.
 */
@Component
public class LabResultEventListener {

    private static final Logger log = LoggerFactory.getLogger(LabResultEventListener.class);

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "emr.lab-result-ready.queue", durable = "true"),
            exchange = @Exchange(name = "hms.events", type = ExchangeTypes.TOPIC, durable = "true"),
            key = "lab.result.ready"
    ))
    public void onLabResultReady(DomainEvent event) {
        log.info("Lab result ready event received: patientId={}, orderId={}, orderItemId={}, testCode={}, abnormalFlag={}",
                event.payload().get("patientId"), event.payload().get("orderId"), event.payload().get("orderItemId"),
                event.payload().get("testCode"), event.payload().get("abnormalFlag"));
    }
}
