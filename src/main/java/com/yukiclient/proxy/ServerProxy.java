package com.yukiclient.proxy;

import com.yukiclient.YukiClient;
import com.yukiclient.badge.ClientBadgeServerHandler;
import net.minecraftforge.fml.common.network.FMLEventChannel;

/**
 * Server-side proxy. Initializes only the presence relay so players running
 * YukiClient can discover each other on LAN or dedicated servers.
 */
public class ServerProxy implements CommonProxy {

    @Override
    public void init(YukiClient mod, FMLEventChannel channel) {
        new ClientBadgeServerHandler(channel).register();
    }
}
