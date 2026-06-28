package com.yukiclient.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.yukiclient.modules.Module;
import com.yukiclient.modules.ModuleManager;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles persistent storage of YukiClient module settings.
 * Settings are saved as pretty-printed JSON in {@code .minecraft/config/yukiclient.json}.
 */
public class ConfigManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String CONFIG_NAME = "yukiclient.json";
    private static final String CONFIG_DIR = "config";

    /** Reserved config key used to persist global HUD settings. */
    private static final String GLOBAL_KEY = "$yukiGlobal";

    private final ModuleManager moduleManager;
    private final File configFile;

    public ConfigManager(ModuleManager moduleManager) {
        this.moduleManager = moduleManager;
        this.configFile = new File(new File(Minecraft.getMinecraft().mcDataDir, CONFIG_DIR), CONFIG_NAME);
    }

    /**
     * Loads saved module settings and applies them to the registered modules.
     * If the config file does not exist or is invalid, the default module state is kept.
     */
    public void load() {
        if (!configFile.exists()) {
            return;
        }

        try (FileReader reader = new FileReader(configFile)) {
            Type type = new TypeToken<Map<String, ModuleConfig>>() {}.getType();
            Map<String, ModuleConfig> configs = GSON.fromJson(reader, type);
            if (configs == null) {
                return;
            }

            // Restore global HUD scale before per-module values so the effective scale is correct.
            ModuleConfig globalConfig = configs.get(GLOBAL_KEY);
            if (globalConfig != null) {
                Module.setGlobalScale(globalConfig.getScale());
            }

            for (Module module : moduleManager.getModules()) {
                ModuleConfig config = configs.get(module.getName());
                if (config != null) {
                    module.setEnabled(config.isEnabled());
                    module.setX(config.getX());
                    module.setY(config.getY());
                    module.setScale(config.getScale());
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to load YukiClient config from " + configFile.getAbsolutePath());
            e.printStackTrace();
        }
    }

    /**
     * Writes the current module states to disk.
     * Missing parent directories are created automatically.
     */
    public void save() {
        try {
            File parent = configFile.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }

            Map<String, ModuleConfig> configs = new HashMap<String, ModuleConfig>();

            // Persist the global HUD scale using the reserved key.
            ModuleConfig globalConfig = new ModuleConfig(GLOBAL_KEY);
            globalConfig.setScale(Module.getGlobalScale());
            configs.put(GLOBAL_KEY, globalConfig);

            for (Module module : moduleManager.getModules()) {
                ModuleConfig config = new ModuleConfig(module.getName());
                config.setEnabled(module.isEnabled());
                config.setX(module.getX());
                config.setY(module.getY());
                config.setScale(module.getScale());
                configs.put(module.getName(), config);
            }

            try (FileWriter writer = new FileWriter(configFile)) {
                GSON.toJson(configs, writer);
            }
        } catch (IOException e) {
            System.err.println("Failed to save YukiClient config to " + configFile.getAbsolutePath());
            e.printStackTrace();
        }
    }
}
