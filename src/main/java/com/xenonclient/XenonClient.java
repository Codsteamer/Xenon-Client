package com.xenonclient;

import com.xenonclient.gui.WheelScreen;
import com.xenonclient.module.ModuleManager;
import com.xenonclient.render.ESPRenderer;
import com.xenonclient.section.SectionManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XenonClient implements ClientModInitializer {

    public static final String MOD_ID = "xenon-client";
    public static final String MOD_NAME = "Xenon Client";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    private static XenonClient instance;
    private SectionManager sectionManager;
    private ModuleManager moduleManager;
    private KeyMapping openGuiKey;

    public static XenonClient getInstance() {
        return instance;
    }

    @Override
    public void onInitializeClient() {
        instance = this;
        LOGGER.info("Xenon Client initializing...");

        sectionManager = new SectionManager();
        sectionManager.registerDefaults();

        moduleManager = new ModuleManager();
        moduleManager.registerDefaults();

        ESPRenderer.register();

        KeyMapping.Category xenonCategory = KeyMapping.Category.register(
                Identifier.parse("xenon-client:xenon")
        );

        openGuiKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.xenon-client.open_gui",
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                xenonCategory
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (openGuiKey.consumeClick()) {
                openWheelGui(client);
            }
        });

        LOGGER.info("Xenon Client initialized!");
    }

    private void openWheelGui(Minecraft client) {
        if (client.screen == null) {
            client.setScreen(new WheelScreen());
        }
    }

    public SectionManager getSectionManager() {
        return sectionManager;
    }

    public ModuleManager getModuleManager() {
        return moduleManager;
    }
}
