package com.yukiclient;

import com.yukiclient.badge.ClientBadgeRenderer;
import com.yukiclient.config.ConfigManager;
import com.yukiclient.event.HudRenderListener;
import com.yukiclient.modules.FreelookModule;
import com.yukiclient.modules.FullBrightModule;
import com.yukiclient.modules.ModuleManager;
import com.yukiclient.modules.SprintModule;
import com.yukiclient.modules.ToggleSprintModule;
import com.yukiclient.modules.ZoomModule;
import com.yukiclient.modules.hud.ArmorStatusModule;
import com.yukiclient.modules.hud.BiomeModule;
import com.yukiclient.modules.hud.ClockModule;
import com.yukiclient.modules.hud.CoordinatesModule;
import com.yukiclient.modules.hud.CpsModule;
import com.yukiclient.modules.hud.FpsModule;
import com.yukiclient.modules.hud.KeystrokesModule;
import com.yukiclient.modules.hud.PingModule;
import com.yukiclient.modules.hud.PotionStatusModule;
import com.yukiclient.modules.hud.SessionTimerModule;
import com.yukiclient.modules.hud.SpeedModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;

@Mod(modid = YukiClient.MODID, name = YukiClient.NAME, version = YukiClient.VERSION)
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
        registerModules(moduleManager);

        ConfigManager configManager = new ConfigManager(moduleManager);
        configManager.load();
        this.configManager = configManager;

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                ConfigManager cm = YukiClient.this.configManager;
                if (cm != null) {
                    cm.save();
                }
            }
        }, "YukiClient Config Save Thread"));

        MinecraftForge.EVENT_BUS.register(new HudRenderListener(moduleManager));

        openGuiKey = new KeyBinding("Open Yuki HUD Editor", Keyboard.KEY_RSHIFT, "YukiClient");
        ClientRegistry.registerKeyBinding(openGuiKey);

        new ClientBadgeRenderer().register();

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void registerModules(ModuleManager moduleManager) {
        moduleManager.registerModule(new FpsModule());

        moduleManager.registerModule(new CoordinatesModule());

        CpsModule cps = new CpsModule();
        moduleManager.registerModule(cps);
        MinecraftForge.EVENT_BUS.register(cps);

        KeystrokesModule keystrokes = new KeystrokesModule();
        moduleManager.registerModule(keystrokes);
        MinecraftForge.EVENT_BUS.register(keystrokes);

        moduleManager.registerModule(new ArmorStatusModule());

        moduleManager.registerModule(new PingModule());
        moduleManager.registerModule(new SpeedModule());
        moduleManager.registerModule(new PotionStatusModule());
        moduleManager.registerModule(new ClockModule());
        moduleManager.registerModule(new SessionTimerModule());
        moduleManager.registerModule(new BiomeModule());

        FreelookModule freelook = new FreelookModule();
        moduleManager.registerModule(freelook);
        MinecraftForge.EVENT_BUS.register(freelook);

        SprintModule sprint = new SprintModule();
        moduleManager.registerModule(sprint);
        MinecraftForge.EVENT_BUS.register(sprint);

        ToggleSprintModule toggleSprint = new ToggleSprintModule();
        moduleManager.registerModule(toggleSprint);
        MinecraftForge.EVENT_BUS.register(toggleSprint);

        ZoomModule zoom = new ZoomModule();
        moduleManager.registerModule(zoom);
        MinecraftForge.EVENT_BUS.register(zoom);

        FullBrightModule fullBright = new FullBrightModule();
        moduleManager.registerModule(fullBright);
        MinecraftForge.EVENT_BUS.register(fullBright);
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (openGuiKey != null && openGuiKey.isPressed()) {
            Minecraft.getMinecraft().displayGuiScreen(new com.yukiclient.gui.ClickGuiScreen(moduleManager, configManager));
        }
    }

    public ModuleManager getModuleManager() {
        return moduleManager;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public void setConfigManager(ConfigManager configManager) {
        this.configManager = configManager;
    }
}
