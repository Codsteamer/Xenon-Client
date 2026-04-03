package com.xenonclient.module;

import com.xenonclient.module.render.BlockESP;
import com.xenonclient.module.render.PlayerESP;
import com.xenonclient.module.render.StorageESP;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Central registry for all Xenon Client modules.
 */
public class ModuleManager {

    private final List<Module> modules = new ArrayList<>();

    public void registerDefaults() {
        register(new BlockESP());
        register(new StorageESP());
        register(new PlayerESP());
    }

    public void register(Module module) {
        modules.add(module);
    }

    public List<Module> getModules() {
        return Collections.unmodifiableList(modules);
    }

    /**
     * Returns all modules belonging to a given section name.
     */
    public List<Module> getModulesBySection(String sectionName) {
        List<Module> result = new ArrayList<>();
        for (Module module : modules) {
            if (module.getSection().equalsIgnoreCase(sectionName)) {
                result.add(module);
            }
        }
        return result;
    }

    public Module getModule(String name) {
        for (Module module : modules) {
            if (module.getName().equalsIgnoreCase(name)) {
                return module;
            }
        }
        return null;
    }
}
