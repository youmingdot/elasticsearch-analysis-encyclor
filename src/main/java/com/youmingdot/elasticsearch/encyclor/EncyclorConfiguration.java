package com.youmingdot.elasticsearch.encyclor;

import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;

/**
 * Encyclor Configuration
 *
 * @author You Ming
 */
public class EncyclorConfiguration {

    private final Settings settings;

    private final Environment environment;

    private final String uri;

    private final boolean ignoreCase;

    private final int updateInterval;

    private final boolean expand;

    private final String format;

    public EncyclorConfiguration(Settings settings, Environment environment) {
        this.settings = settings;
        this.environment = environment;

        uri = settings.get("dictionary");

        if (uri == null) {
            throw new IllegalArgumentException("Configuration [dictionary] must be specified.");
        }

        ignoreCase = settings.getAsBoolean("ignore_case", false);
        updateInterval = settings.getAsInt("interval", 60);
        expand = settings.getAsBoolean("expand", true);
        format = settings.get("format", "");
    }

    public Settings getSettings() {
        return settings;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public String getUri() {
        return uri;
    }

    public boolean isIgnoreCase() {
        return ignoreCase;
    }

    public int getUpdateInterval() {
        return updateInterval;
    }

    public boolean isExpand() {
        return expand;
    }

    public String getFormat() {
        return format;
    }
}
