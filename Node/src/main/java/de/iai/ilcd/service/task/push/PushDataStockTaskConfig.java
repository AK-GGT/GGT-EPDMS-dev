package de.iai.ilcd.service.task.push;

import de.iai.ilcd.model.common.PushConfig;

public class PushDataStockTaskConfig {

    private PushConfig pushConfig;

    private String password;

    public PushDataStockTaskConfig(PushConfig pushConfig, String password) {
        this.pushConfig = pushConfig;
        this.password = password;
    }

    public PushConfig getPushConfig() {
        return pushConfig;
    }

    public void setPushConfig(PushConfig pushConfig) {
        this.pushConfig = pushConfig;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
