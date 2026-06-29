package com.yukiclient.modules.hud;

import com.yukiclient.modules.Module;
import com.yukiclient.theme.YukiTheme;
import net.minecraft.client.Minecraft;
import net.minecraft.util.MathHelper;

/**
 * Coordinates Module - LunarClient Style.
 *
 * Displays the player's current block position (X / Y / Z) and the cardinal
 * direction they are facing inside a dark bordered HUD box.
 */
public class CoordinatesModule extends Module {
    private final Minecraft mc = Minecraft.getMinecraft();

    private final int paddingX = 6;
    private final int paddingY = 4;
    private final int lineGap = 2;
    private static final int LINE_HEIGHT = 8; // font height ~8

    public CoordinatesModule() {
        super("Coordinates", "Displays your current X / Y / Z position and facing.", Module.Category.HUD);
        // Default position: top area, to the right of the FPS box.
        this.x = 100;
        this.y = 10;
        this.width = 80;
        this.height = 40;
    }

    @Override
    public void render() {
        // Safety check to prevent crashes when the player is not in a world.
        if (mc.thePlayer == null) {
            return;
        }

        int blockX = MathHelper.floor_double(mc.thePlayer.posX);
        int blockY = MathHelper.floor_double(mc.thePlayer.posY);
        int blockZ = MathHelper.floor_double(mc.thePlayer.posZ);

        String[] lines = {
                "X: " + blockX,
                "Y: " + blockY,
                "Z: " + blockZ,
                "Facing: " + getFacing(mc.thePlayer.rotationYaw)
        };

        int maxTextWidth = 0;
        for (String line : lines) {
            int w = mc.fontRendererObj.getStringWidth(line);
            if (w > maxTextWidth) {
                maxTextWidth = w;
            }
        }

        int boxWidth = maxTextWidth + paddingX * 2;
        int boxHeight = lines.length * LINE_HEIGHT + (lines.length - 1) * lineGap + paddingY * 2;

        // Draw frosted Japanese-snow panel.
        YukiTheme.drawFrostPanel(this.x, this.y, boxWidth, boxHeight);

        int textX = this.x + paddingX;
        int textY = this.y + paddingY;
        for (int i = 0; i < lines.length; i++) {
            // Coordinates in snow-white; the facing line in frost-blue for contrast.
            int color = (i == lines.length - 1) ? YukiTheme.FROST_BLUE : YukiTheme.SNOW_WHITE;
            mc.fontRendererObj.drawString(lines[i], textX, textY, color, false);
            textY += LINE_HEIGHT + lineGap;
        }

        // Update dimensions for the GUI editor.
        this.width = boxWidth;
        this.height = boxHeight;
    }

    /**
     * Converts a yaw angle into a readable cardinal direction with its axis sign.
     */
    private String getFacing(float yaw) {
        int dir = MathHelper.floor_double((double) (yaw * 4.0F / 360.0F) + 0.5D) & 3;
        switch (dir) {
            case 0:
                return "South (+Z)";
            case 1:
                return "West (-X)";
            case 2:
                return "North (-Z)";
            case 3:
                return "East (+X)";
            default:
                return "?";
        }
    }
}
