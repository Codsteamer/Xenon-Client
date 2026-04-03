package com.xenonclient.module;

import com.xenonclient.module.movement.SprintModule;
import com.xenonclient.module.render.BlockESPModule;
import com.xenonclient.module.render.FullbrightModule;
import com.xenonclient.module.render.PlayerESPModule;
import com.xenonclient.module.render.StorageESPModule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Manages all registered modules for Xenon Client.
 */
public class ModuleManager {

    private final List<Module> modules = new ArrayList<>();

    public void registerDefaults() {
        register(new SprintModule());
        register(new FullbrightModule());
        register(new BlockESPModule());
        register(new StorageESPModule());
        register(new PlayerESPModule());
    }

    public void register(Module module) {
        modules.add(module);
    }

    public List<Module> getModules() {
        return Collections.unmodifiableList(modules);
    }

    public List<Module> getModulesForSection(String sectionName) {
        List<Module> result = new ArrayList<>();
        for (Module module : modules) {
            if (module.getSectionName().equalsIgnoreCase(sectionName)) {
                result.add(module);
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public <T extends Module> T getModule(Class<T> clazz) {
        for (Module module : modules) {
            if (clazz.isInstance(module)) {
                return (T) module;
            }
        }
        return null;
    }
}
