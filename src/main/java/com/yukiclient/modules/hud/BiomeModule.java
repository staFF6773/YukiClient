package com.yukiclient.modules.hud;

import com.yukiclient.modules.Module;
import com.yukiclient.theme.YukiTheme;
import net.minecraft.client.Minecraft;
import net.minecraft.util.BlockPos;
import net.minecraft.world.biome.BiomeGenBase;

/**
 * Biome Module.
 *
 * Displays the name of the biome the player is currently standing in.
 */
public class BiomeModule extends Module {
    private final Minecraft mc = Minecraft.getMinecraft();

    private final int paddingX = 6;
    private final int paddingY = 4;

    public BiomeModule() {
        super("Biome", "Shows the biome you are currently standing in.", Module.Category.HUD);
        this.enabled = false;
        this.x = 200;
        this.y = 80;
        this.width = 80;
        this.height = 16;
    }

    @Override
    public void render() {
        String biome = "Unknown";
        if (mc.thePlayer != null && mc.theWorld != null) {
            try {
                BiomeGenBase b = mc.theWorld.getBiomeGenForCoords(new BlockPos(mc.thePlayer));
                if (b != null && b.biomeName != null) {
                    biome = b.biomeName;
                }
            } catch (Exception ignored) {
                // Defensive: never let a HUD readout crash the render loop.
            }
        }
        String text = "Biome: " + biome;

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
