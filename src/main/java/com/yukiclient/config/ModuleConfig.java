package com.yukiclient.config;

/**
 * Plain data object that holds persistent state for a single module.
 * This class is serialized to/from JSON by {@link ConfigManager}.
 */
public class ModuleConfig {

    private final String name;
    private boolean enabled;
    private int x;
    private int y;
    private float scale = 1.0f;

    /**
     * No-arg constructor required for Gson deserialization.
     */
    public ModuleConfig() {
        this.name = "";
    }

    public ModuleConfig(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }
}
