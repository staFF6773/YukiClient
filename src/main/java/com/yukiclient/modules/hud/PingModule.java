package com.yukiclient.modules.hud;

import com.yukiclient.modules.Module;
import com.yukiclient.theme.YukiTheme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;

/**
 * Ping Module.
 *
 * Displays the local player's latency (round-trip time) to the current server,
 * read from the tab-list player info. Shows 0ms in single-player.
 */
public class PingModule extends Module {
    private final Minecraft mc = Minecraft.getMinecraft();

    private final int paddingX = 6;
    private final int paddingY = 4;

    public PingModule() {
        super("Ping", "Shows your latency to the server.", Module.Category.HUD);
        this.x = 100;
        this.y = 70;
        this.width = 60;
        this.height = 16;
    }

    @Override
    public void render() {
        int ping = getPing();
        String text = "Ping: " + ping + "ms";

        int textWidth = mc.fontRendererObj.getStringWidth(text);
        int boxWidth = textWidth + paddingX * 2;
        int boxHeight = 8 + paddingY * 2;

        YukiTheme.drawFrostPanel(this.x, this.y, boxWidth, boxHeight);

        int textX = this.x + 2 + (boxWidth - 2 - textWidth) / 2;
        int textY = this.y + paddingY;
        YukiTheme.drawStringWithFrostShadow(text, textX, textY, YukiTheme.SNOW_WHITE);

        // Update dimensions for the GUI editor.
        this.width = boxWidth;
        this.height = boxHeight;
    }

    /**
     * Resolves the local player's ping from the connection's player-info list.
     * Returns 0 when offline or when the info is not yet available.
     */
    private int getPing() {
        if (mc.thePlayer == null || mc.getNetHandler() == null) {
            return 0;
        }
        NetworkPlayerInfo info = mc.getNetHandler().getPlayerInfo(mc.thePlayer.getGameProfile().getId());
        return info != null ? info.getResponseTime() : 0;
    }
}
