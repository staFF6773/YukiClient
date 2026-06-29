package com.yukiclient.modules.hud;

import com.yukiclient.modules.Module;
import com.yukiclient.theme.YukiTheme;
import net.minecraft.client.Minecraft;

/**
 * Session Timer Module.
 *
 * Shows how long the current game session has been running, formatted as
 * mm:ss (or h:mm:ss once it passes an hour).
 */
public class SessionTimerModule extends Module {
    private final Minecraft mc = Minecraft.getMinecraft();
    private final long startTime = System.currentTimeMillis();

    private final int paddingX = 6;
    private final int paddingY = 4;

    public SessionTimerModule() {
        super("SessionTimer", "Shows how long this session has been running.", Module.Category.HUD);
        this.enabled = false;
        this.x = 200;
        this.y = 60;
        this.width = 70;
        this.height = 16;
    }

    @Override
    public void render() {
        long elapsed = (System.currentTimeMillis() - startTime) / 1000L;
        long hours = elapsed / 3600L;
        long minutes = (elapsed % 3600L) / 60L;
        long seconds = elapsed % 60L;
        String time = hours > 0
                ? String.format("%d:%02d:%02d", hours, minutes, seconds)
                : String.format("%02d:%02d", minutes, seconds);
        String text = "Session " + time;

        int textWidth = mc.fontRendererObj.getStringWidth(text);
        int boxWidth = textWidth + paddingX * 2;
        int boxHeight = 8 + paddingY * 2;

        YukiTheme.drawFrostPanel(this.x, this.y, boxWidth, boxHeight);

        int textX = this.x + 2 + (boxWidth - 2 - textWidth) / 2;
        int textY = this.y + paddingY;
        YukiTheme.drawStringWithFrostShadow(text, textX, textY, YukiTheme.SNOW_WHITE);

        this.width = boxWidth;
        this.height = boxHeight;
    }
}
