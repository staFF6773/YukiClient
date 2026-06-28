package com.yukiclient.proxy;

import com.yukiclient.YukiClient;
import net.minecraftforge.fml.common.network.FMLEventChannel;

/**
 * Common proxy interface for YukiClient. Forge injects the client or server
 * implementation depending on the current side.
 */
public interface CommonProxy {

    /**
     * Called during {@code FMLInitializationEvent}.
     *
     * @param mod     the YukiClient mod instance.
     * @param channel the shared plugin-message channel for badge presence.
     */
    void init(YukiClient mod, FMLEventChannel channel);
}
