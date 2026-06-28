package com.yukiclient.modules.hud;

import com.yukiclient.YukiClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import org.lwjgl.opengl.GL11;

/**
 * Renders the YukiClient snowflake watermark and version info
 * in the bottom-right corner. It is not a Module, so it cannot
 * be toggled, moved, or edited.
 */
public final class YukiLogo {

    private static final ResourceLocation YUKI_TEXTURE =
            new ResourceLocation("yukiclient", "yuki.png");

    private static final int SIZE = 16;
    private static final int PADDING = 10;
    private static final int TEXT_GAP = 5;
    private static final float ALPHA = 0.85F;

    private static final String VERSION_TEXT = YukiClient.NAME + " v" + YukiClient.VERSION;
    private static final int TEXT_COLOR = 0xFFFFFF;

    private static int cachedTextWidth = -1;
    private static int lastScreenWidth = -1;
    private static int lastScreenHeight = -1;

    private static int logoX;
    private static int logoY;
    private static int textX;
    private static int textY;

    /**
     * Draws the client version text and the snowflake logo in the
     * bottom-right corner of the screen.
     *
     * @param event the Forge post-render overlay event.
     */
    public static void render(RenderGameOverlayEvent.Post event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.getTextureManager() == null || mc.fontRendererObj == null) {
            return;
        }

        int screenWidth = event.resolution.getScaledWidth();
        int screenHeight = event.resolution.getScaledHeight();
        FontRenderer fr = mc.fontRendererObj;

        // Cache layout values; only recompute on resolution change.
        if (cachedTextWidth == -1) {
            cachedTextWidth = fr.getStringWidth(VERSION_TEXT);
        }
        if (screenWidth != lastScreenWidth || screenHeight != lastScreenHeight) {
            lastScreenWidth = screenWidth;
            lastScreenHeight = screenHeight;

            logoX = screenWidth - SIZE - PADDING;
            logoY = screenHeight - SIZE - PADDING;
            textX = logoX - TEXT_GAP - cachedTextWidth;
            textY = logoY + (SIZE - fr.FONT_HEIGHT) / 2;
        }

        // Single blend enable for both logo and text.
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(
                GL11.GL_SRC_ALPHA,
                GL11.GL_ONE_MINUS_SRC_ALPHA,
                GL11.GL_ONE,
                GL11.GL_ZERO);

        // Draw translucent snowflake.
        GlStateManager.color(1.0F, 1.0F, 1.0F, ALPHA);
        mc.getTextureManager().bindTexture(YUKI_TEXTURE);
        Gui.drawModalRectWithCustomSizedTexture(logoX, logoY, 0.0F, 0.0F, SIZE, SIZE, SIZE, SIZE);

        // Draw version text using the same blend state.
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        fr.drawString(VERSION_TEXT, textX, textY, TEXT_COLOR, true);

        GlStateManager.disableBlend();
    }
}
