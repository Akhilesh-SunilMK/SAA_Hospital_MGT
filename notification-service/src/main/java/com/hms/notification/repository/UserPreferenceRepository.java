package com.hms.notification.repository;

import com.hms.notification.entity.UserPreference;
import com.hms.notification.model.ChannelType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserPreferenceRepository extends JpaRepository<UserPreference, Long> {
    Optional<UserPreference> findByUserIdAndChannel(Long userId, ChannelType channel);
}
