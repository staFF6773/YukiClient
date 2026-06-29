package com.yukiclient.modules.hud;

import com.yukiclient.modules.Module;
import com.yukiclient.theme.YukiTheme;
import net.minecraft.client.Minecraft;

/**
 * FPS Counter Module - LunarClient Style.
 * Displays the current frames per second inside a dark bordered HUD box.
 */
public class FpsModule extends Module {
    private final Minecraft mc = Minecraft.getMinecraft();

    private final int paddingX = 6;
    private final int paddingY = 4;

    // Cache the formatted FPS string to avoid allocating every frame.
    private int lastFps = -1;
    private String fpsText = "";

    public FpsModule() {
        super("FPS", "Displays current frames per second.", Module.Category.HUD);
        // Default position: top-left corner
        this.x = 10;
        this.y = 10;
        this.width = 60;
        this.height = 16;
    }

    @Override
    public void render() {
        int fps = mc.getDebugFPS();
        if (fps != lastFps) {
            lastFps = fps;
            fpsText = "FPS: " + fps;
        }

        int textWidth = mc.fontRendererObj.getStringWidth(fpsText);
        int boxWidth = textWidth + paddingX * 2;
        int boxHeight = 8 + paddingY * 2; // font height ~8

        // Draw frosted Japanese-snow panel
        YukiTheme.drawFrostPanel(this.x, this.y, boxWidth, boxHeight);

        // Center text within the area past the left accent bar.
        int textX = this.x + 2 + (boxWidth - 2 - textWidth) / 2;
        int textY = this.y + paddingY;
        YukiTheme.drawStringWithFrostShadow(fpsText, textX, textY, YukiTheme.SNOW_WHITE);

        // Update dimensions for the GUI editor
        this.width = boxWidth;
        this.height = boxHeight;
    }
}
