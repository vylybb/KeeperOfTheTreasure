package ch.treasurekeep.rest;

import ch.treasurekeep.data.LogRepository;
import ch.treasurekeep.model.CostThreshold;
import ch.treasurekeep.model.Log;
import ch.treasurekeep.model.NetvalueThreshold;
import ch.treasurekeep.model.Settings;
import ch.treasurekeep.service.SettingsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Offers all REST-Calls concerning the Settings-Object
 * (A singleton-object in the Mongo-Database)
 */
@RestController
@RequestMapping("/")
public class SettingsController {

    private final SettingsService settingsService;
    private final LogRepository logRepository;

    public SettingsController(SettingsService settingsService, LogRepository logRepository) {
        this.settingsService = settingsService;
        this.logRepository = logRepository;
    }

    @GetMapping("/settings")
    public Settings getSettings() {
        return this.settingsService.getSettings();
    }

    @PutMapping("/settingsstring")
    public void setSettingsString(String json) {
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            this.settingsService.setSettings(objectMapper.readValue(json, Settings.class));
        }
        catch (Exception e ) {
            this.logRepository.insert(new Log(SettingsController.class.getName(), e.getMessage()));
        }
    }
    @GetMapping("/settingsstring")
    public String getSettingsString() throws Exception {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.writerWithDefaultPrettyPrinter();
            return objectMapper.writeValueAsString(this.settingsService.getSettings());
        }
        catch (Exception e ) {
            this.logRepository.insert(new Log(SettingsController.class.getName(), e.getMessage()));
            return e.getMessage();
        }
    }

    @PostMapping("/costthreshold")
    public synchronized void createCostThreshold(CostThreshold threshold) {
       try{
           if(StringUtils.isEmpty(threshold.getId()) || "{}".equals(threshold.getId())) {
               threshold.setId(UUID.randomUUID().toString());
           }
           Settings clone = new Settings(this.settingsService.getSettings());
           clone.getCostThresholds().add(threshold);
           this.settingsService.setSettings(clone);
       }
       catch (Exception e) {
           this.logRepository.insert(new Log(SettingsController.class.getName(), e.getMessage()));
       }
    }

    @PutMapping("/costthreshold")
    public synchronized void updateCostThreshold(CostThreshold costThreshold) {
        try {
            Settings settings = this.settingsService.getSettings();

            List<CostThreshold> replaceThis = settings.getCostThresholds().stream()
                    .filter(o -> o.getId().equals(costThreshold.getId()))
                    .collect(Collectors.toList());
            if (replaceThis.size() != 1) {
                throw new IllegalStateException("costthreshold with id " + costThreshold.getId() + " has no or more than one candidate");
            }
            settings.getCostThresholds().remove(replaceThis.get(0));
            settings.getCostThresholds().add(costThreshold);
            this.settingsService.setSettings(settings);
        }
        catch (Exception e) {
            this.logRepository.insert(new Log(SettingsController.class.getName(), e.getMessage()));
        }
    }

    @DeleteMapping("/costthreshhold")
    public synchronized void deleteCostThreshold(@RequestParam String id) {
        Settings settings = this.settingsService.getSettings();
        settings.getCostThresholds().remove(settings.getCostThresholds().stream().filter(o -> o.getId().equals(id)).collect(Collectors.toList()).get(0));
        this.settingsService.setSettings(settings);
    }

    @PostMapping("/netvaluethreshold")
    public synchronized void createNetValueThreshold(NetvalueThreshold threshold) {
        if(StringUtils.isEmpty(threshold.getId()) || "{}".equals(threshold.getId())) {
            threshold.setId(UUID.randomUUID().toString());
        }
        Settings clone = new Settings(this.settingsService.getSettings());
        clone.getNetvalueThresholds().add(threshold);
        this.settingsService.setSettings(clone);
    }

    @PutMapping("netvaluethreshold")
    public synchronized void updateNetValueThreshhold(NetvalueThreshold threshold) {
        try {
            Settings settings = this.settingsService.getSettings();
            List<NetvalueThreshold> targetThreshollds = settings.getNetvalueThresholds()
                    .stream()
                    .filter(o -> o.getId().equals(threshold.getId()))
                    .collect(Collectors.toList());
            if (targetThreshollds.size() != 1) {
                throw new IllegalStateException("More than on or zero entries are matching NetvalueThreshold with id of " + threshold.getId());
            }
            settings.getNetvalueThresholds().remove(targetThreshollds.get(0));
            settings.getNetvalueThresholds().add(threshold);
            this.settingsService.setSettings(settings);
        }
        catch(Exception e) {
            this.logRepository.insert(new Log(SettingsController.class.getName(), e.getMessage()));
        }
    }
    @DeleteMapping("/netvaluetreshhold")
    public synchronized void deleteNetValueThreshold(@RequestParam String id) {
        Settings settings = this.settingsService.getSettings();
        settings.getNetvalueThresholds().remove(settings.getNetvalueThresholds().stream().filter(o -> o.getId().equals(id)).collect(Collectors.toList()).get(0));
        this.settingsService.setSettings(settings);
    }

    @PutMapping("reconnecting")
    public synchronized void setReconnecting(boolean reconnecting) {
        Settings settings = this.settingsService.getSettings();
        settings.setReconnecting(reconnecting);;
        this.settingsService.setSettings(settings);
    }

    @PutMapping("activeaccounts")
    public synchronized void setActiveAccounts(List<String> activeAccounts) {
        Settings settings = this.settingsService.getSettings();
        settings.getManagedAccounts().clear();
        settings.getManagedAccounts().addAll(activeAccounts);
        this.settingsService.setSettings(settings);
    }

    @GetMapping("managedaccounts")
    public synchronized  List<String> getActiveAccounts() {
        return this.settingsService.getSettings().getManagedAccounts();
    }

}
