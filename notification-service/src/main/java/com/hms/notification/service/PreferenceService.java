package com.hms.notification.service;

import com.hms.notification.entity.UserPreference;
import com.hms.notification.model.ChannelType;
import com.hms.notification.repository.UserPreferenceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** FR-NT-07: per-user channel preferences and opt-outs. */
@Service
public class PreferenceService {

    private final UserPreferenceRepository repository;

    public PreferenceService(UserPreferenceRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public UserPreference upsert(Long userId, ChannelType channel, boolean enabled) {
        UserPreference preference = repository.findByUserIdAndChannel(userId, channel)
                .orElseGet(() -> new UserPreference(userId, channel, enabled));
        preference.updateEnabled(enabled);
        return repository.save(preference);
    }
}
