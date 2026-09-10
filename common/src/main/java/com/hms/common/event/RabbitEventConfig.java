package com.hms.common.event;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Declares the shared "hms.events" topic exchange. Only activates for services that pull in
 * spring-boot-starter-amqp themselves (publishers: appointment, lab, pharmacy, billing;
 * consumers: notification, emr) — services without it on the classpath (auth, patient, doctor,
 * gateway) skip this configuration entirely.
 */
@Configuration
@ConditionalOnClass(RabbitTemplate.class)
public class RabbitEventConfig {

    public static final String EXCHANGE_NAME = "hms.events";

    @Bean
    public TopicExchange hmsEventsExchange() {
        return new TopicExchange(EXCHANGE_NAME, true, false);
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(org.springframework.amqp.rabbit.connection.ConnectionFactory connectionFactory,
                                          Jackson2JsonMessageConverter converter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(converter);
        return template;
    }
}
