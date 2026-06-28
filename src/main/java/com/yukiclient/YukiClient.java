package com.yukiclient;

import com.yukiclient.config.ConfigManager;
import com.yukiclient.modules.ModuleManager;
import com.yukiclient.proxy.CommonProxy;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.network.FMLEventChannel;
import net.minecraftforge.fml.common.network.NetworkRegistry;

/**
 * Main entry point for the YukiClient mod.
 * The {@link CommonProxy} implementation handles side-specific initialization.
 */
@Mod(modid = YukiClient.MODID, name = YukiClient.NAME, version = YukiClient.VERSION)
public class YukiClient {

    public static final String MODID = "yukiclient";
    public static final String VERSION = "1.0.0";
    public static final String NAME = "YukiClient";

    @SidedProxy(clientSide = "com.yukiclient.proxy.ClientProxy", serverSide = "com.yukiclient.proxy.ServerProxy")
    public static CommonProxy proxy;

    private static YukiClient instance;

    private ModuleManager moduleManager;
    private ConfigManager configManager;

    public static YukiClient getInstance() {
        return instance;
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        instance = this;

        moduleManager = new ModuleManager();

        FMLEventChannel channel = NetworkRegistry.INSTANCE.newEventDrivenChannel(com.yukiclient.badge.BadgeChannel.CHANNEL);
        proxy.init(this, channel);

        // Register self on the Forge event bus for any common events.
        MinecraftForge.EVENT_BUS.register(this);
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
