package com.yukiclient.modules.hud;

import com.yukiclient.modules.Module;
import com.yukiclient.theme.YukiTheme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;

/**
 * Speed Module.
 *
 * Displays the player's horizontal movement speed in blocks per second (BPS),
 * derived from the position delta of the last tick. Useful for tracking
 * sprint-jump and knockback speed in PvP.
 */
public class SpeedModule extends Module {
    private final Minecraft mc = Minecraft.getMinecraft();

    private final int paddingX = 6;
    private final int paddingY = 4;

    public SpeedModule() {
        super("Speed", "Shows your horizontal speed in blocks per second.", Module.Category.HUD);
        // Off by default to keep the stock HUD uncluttered.
        this.enabled = false;
        this.x = 100;
        this.y = 90;
        this.width = 60;
        this.height = 16;
    }

    @Override
    public void render() {
        double bps = 0.0;
        EntityPlayerSP player = mc.thePlayer;
        if (player != null) {
            double dx = player.posX - player.prevPosX;
            double dz = player.posZ - player.prevPosZ;
            bps = Math.sqrt(dx * dx + dz * dz) * 20.0; // 20 ticks per second
        }
        String text = String.format("%.2f BPS", bps);

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
}
