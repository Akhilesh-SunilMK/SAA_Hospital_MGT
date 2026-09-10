package com.hms.notification.repository;

import com.hms.notification.entity.NotificationTemplate;
import com.hms.notification.model.ChannelType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, Long> {
    Optional<NotificationTemplate> findByCodeAndChannelAndActiveTrue(String code, ChannelType channel);
}
