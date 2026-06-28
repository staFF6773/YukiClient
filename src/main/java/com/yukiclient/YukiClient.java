package com.yukiclient;

import com.yukiclient.config.ConfigManager;
import com.yukiclient.event.HudRenderListener;
import com.yukiclient.gui.ClickGuiScreen;
import com.yukiclient.gui.GuiEditScreen;
import com.yukiclient.modules.FreelookModule;
import com.yukiclient.modules.FullBrightModule;
import com.yukiclient.modules.ModuleManager;
import com.yukiclient.modules.SprintModule;
import com.yukiclient.modules.ZoomModule;
import com.yukiclient.modules.hud.ArmorStatusModule;
import com.yukiclient.modules.hud.CpsModule;
import com.yukiclient.modules.hud.FpsModule;
import com.yukiclient.modules.hud.KeystrokesModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;

/**
 * Main entry point for the YukiClient mod.
 * Initialized as a Forge mod and acts as the Singleton instance for the client.
 */
@Mod(modid = YukiClient.MODID, name = YukiClient.NAME, version = YukiClient.VERSION, clientSideOnly = true)
public class YukiClient {

    public static final String MODID = "yukiclient";
    public static final String VERSION = "1.0.0";
    public static final String NAME = "YukiClient";

    private static YukiClient instance;
    private ModuleManager moduleManager;
    private ConfigManager configManager;
    private KeyBinding openGuiKey;

    public static YukiClient getInstance() {
        return instance;
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        instance = this;

        moduleManager = new ModuleManager();
        registerModules();

        configManager = new ConfigManager(moduleManager);
        configManager.load();

        // Save settings automatically when the JVM shuts down (e.g. game exit)
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                configManager.save();
            }
        }, "YukiClient Config Save Thread"));

        // Register the HUD renderer as an event listener on the Forge bus
        MinecraftForge.EVENT_BUS.register(new HudRenderListener(moduleManager));
        // Register self to handle key inputs
        MinecraftForge.EVENT_BUS.register(this);

        // Setup keybind for the HUD Editor (Default: Right Shift)
        openGuiKey = new KeyBinding("Open Yuki HUD Editor", Keyboard.KEY_RSHIFT, "YukiClient");
        ClientRegistry.registerKeyBinding(openGuiKey);
    }

    /**
     * Instantiates and registers all core PvP HUD and behavior modules.
     */
    private void registerModules() {
        moduleManager.registerModule(new FpsModule());

        CpsModule cps = new CpsModule();
        moduleManager.registerModule(cps);
        MinecraftForge.EVENT_BUS.register(cps);

        KeystrokesModule keystrokes = new KeystrokesModule();
        moduleManager.registerModule(keystrokes);
        MinecraftForge.EVENT_BUS.register(keystrokes);

        moduleManager.registerModule(new ArmorStatusModule());

        // Freelook is a behavior module: register both in the manager and on the event bus.
        FreelookModule freelook = new FreelookModule();
        moduleManager.registerModule(freelook);
        MinecraftForge.EVENT_BUS.register(freelook);

        // Sprint and Zoom are also behavior modules.
        SprintModule sprint = new SprintModule();
        moduleManager.registerModule(sprint);
        MinecraftForge.EVENT_BUS.register(sprint);

        ZoomModule zoom = new ZoomModule();
        moduleManager.registerModule(zoom);
        MinecraftForge.EVENT_BUS.register(zoom);

        FullBrightModule fullBright = new FullBrightModule();
        moduleManager.registerModule(fullBright);
        MinecraftForge.EVENT_BUS.register(fullBright);
    }

    /**
     * Listens for the custom keybind to open the draggable HUD editor.
     */
    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (openGuiKey.isPressed()) {
            Minecraft.getMinecraft().displayGuiScreen(new ClickGuiScreen(moduleManager, configManager));
        }
    }

    public ModuleManager getModuleManager() {
        return moduleManager;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }
}