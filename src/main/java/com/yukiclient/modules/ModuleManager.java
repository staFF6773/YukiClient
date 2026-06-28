package com.yukiclient.modules;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages the lifecycle and state of all registered YukiClient modules.
 * Provides methods to register, retrieve, and toggle modules.
 */
public class ModuleManager {

    private final ArrayList<Module> modules = new ArrayList<Module>();

    /**
     * Registers a module to the manager.
     *
     * @param module The module to register.
     */
    public void registerModule(Module module) {
        modules.add(module);
    }

    /**
     * Finds and toggles a module by its name (case-insensitive).
     *
     * @param name The name of the module to toggle.
     */
    public void toggleModule(String name) {
        for (Module mod : modules) {
            if (mod.getName().equalsIgnoreCase(name)) {
                mod.toggle();
                return;
            }
        }
    }

    /**
     * Retrieves a module by its name.
     *
     * @param name The name of the module.
     * @return The module, or null if not found.
     */
    public Module getModule(String name) {
        for (Module mod : modules) {
            if (mod.getName().equalsIgnoreCase(name)) {
                return mod;
            }
        }
        return null;
    }

    /**
     * @return A copy of the list of all registered modules.
     */
    public ArrayList<Module> getModules() {
        return new ArrayList<Module>(modules);
    }

    /**
     * @return The internal module list. Callers must NOT modify this list.
     * Intended for hot paths such as the render loop to avoid allocation.
     */
    public ArrayList<Module> getModulesView() {
        return modules;
    }

    /**
     * @return A list of all currently enabled modules.
     */
    public List<Module> getEnabledModules() {
        List<Module> enabled = new ArrayList<Module>();
        for (Module mod : modules) {
            if (mod.isEnabled()) {
                enabled.add(mod);
            }
        }
        return enabled;
    }
}