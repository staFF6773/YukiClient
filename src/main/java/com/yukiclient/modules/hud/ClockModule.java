package com.yukiclient.modules.hud;

import com.yukiclient.modules.Module;
import com.yukiclient.theme.YukiTheme;
import net.minecraft.client.Minecraft;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Clock Module.
 *
 * Displays the current real-world time (HH:mm:ss) in a frosted panel.
 */
public class ClockModule extends Module {
    private final Minecraft mc = Minecraft.getMinecraft();
    private final SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");

    private final int paddingX = 6;
    private final int paddingY = 4;

    public ClockModule() {
        super("Clock", "Shows the current real-world time.", Module.Category.HUD);
        this.enabled = false;
        this.x = 200;
        this.y = 40;
        this.width = 60;
        this.height = 16;
    }

    @Override
    public void render() {
        String text = format.format(new Date());

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
