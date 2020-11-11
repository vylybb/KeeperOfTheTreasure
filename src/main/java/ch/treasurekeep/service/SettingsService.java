package ch.treasurekeep.service;

import ch.treasurekeep.data.SettingsRepository;
import ch.treasurekeep.model.NetvalueThreshold;
import ch.treasurekeep.model.Settings;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Loading and storing of Settings
 * Main purpose is to ensure the Singleton-Nature of the Settings object.
 * And that a Settings object is validated b4 written to Mongo
 */
@Service
public class SettingsService {

    private SettingsRepository repository;

    public SettingsService(SettingsRepository settingsRepository) {
        this.repository = settingsRepository;
    }

    public synchronized Settings getSettings() {
        List<Settings> settings = repository.findAll();
        if(settings.size() == 0) {
            this.setSettings(new Settings());
        }
        if(settings.size() > 1) {
            throw new IllegalStateException();
        }
        return settings.get(0);
    }

    public synchronized void setSettings(Settings settings) {
        List<String> validations = settings.validate();
        if(validations.size() > 0) {
            throw new IllegalStateException(validations.stream().collect(Collectors.joining(",")));
        }
        this.repository.save(settings);
    }
}
