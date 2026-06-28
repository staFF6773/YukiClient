package com.yukiclient.modules;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

/**
 * Zoom Module.
 *
 * Hold C to smoothly zoom in. While zoomed, the FOV shrinks, mouse sensitivity
 * is reduced, and a subtle vignette overlay is drawn around the screen edges.
 * Releasing C smoothly zooms back out.
 *
 * <p>Key handling runs on {@link TickEvent.ClientTickEvent} while smoothing,
 * FOV, and sensitivity interpolation stay on {@link TickEvent.RenderTickEvent}
 * for visual fluidity.</p>
 */
public class ZoomModule extends Module {
    private final Minecraft mc = Minecraft.getMinecraft();

    private boolean wasZoomKeyDown = false;
    private boolean zoomActive = false;

    private float zoomProgress = 0.0F;
    private final float zoomSpeed = 0.10F;      // progress added/removed per render frame
    private final float zoomFactor = 0.25F;     // final FOV multiplier at full zoom
    private final float sensitivityReduction = 0.60F;

    private float originalSensitivity = -1.0F;

    // Cached vignette metrics; only recomputed when the screen resolution changes.
    private int lastScreenW = -1;
    private int lastScreenH = -1;
    private double cachedMaxDist = -1.0D;

    public ZoomModule() {
        super("Zoom", "Hold C for a smooth zoom with reduced mouse sensitivity.");
        this.x = 10;
        this.y = 102;
        this.width = 60;
        this.height = 12;
    }

    @Override
    public void render() {
        // Zoom has no permanent HUD visuals.
    }

    /**
     * Samples the zoom key once per tick and toggles the zoom state.
     */
    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) {
            return;
        }

        // If the module is disabled, the player is not in a world, or a GUI is
        // open, force zoom out and restore sensitivity.
        if (!isEnabled() || mc.thePlayer == null || mc.currentScreen != null) {
            endZoom();
            return;
        }

        boolean zoomKeyDown = Keyboard.isKeyDown(Keyboard.KEY_C);

        if (zoomKeyDown && !wasZoomKeyDown) {
            startZoom();
        } else if (!zoomKeyDown && wasZoomKeyDown) {
            zoomActive = false;
        }
        wasZoomKeyDown = zoomKeyDown;
    }

    /**
     * Smoothly interpolates zoom progress and applies sensitivity on the render path.
     */
    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event) {
        if (event.phase != TickEvent.Phase.START) {
            return;
        }

        if (!isEnabled() || mc.thePlayer == null || mc.currentScreen != null) {
            endZoom();
            return;
        }

        // Smoothly interpolate zoom progress towards the target.
        float target = zoomActive ? 1.0F : 0.0F;
        if (zoomProgress < target) {
            zoomProgress = Math.min(1.0F, zoomProgress + zoomSpeed);
        } else if (zoomProgress > target) {
            zoomProgress = Math.max(0.0F, zoomProgress - zoomSpeed);
        }

        // Zoom is fully closed: restore original sensitivity.
        if (zoomProgress == 0.0F && !zoomActive) {
            endZoom();
        } else if (originalSensitivity >= 0.0F) {
            float multiplier = 1.0F - zoomProgress * sensitivityReduction;
            mc.gameSettings.mouseSensitivity = originalSensitivity * multiplier;
        }
    }

    /**
     * Applies the zoom multiplier to the FOV returned by the game.
     */
    @SubscribeEvent
    public void onFovUpdate(EntityViewRenderEvent.FOVModifier event) {
        if (!isEnabled() || zoomProgress <= 0.0F) return;
        event.setFOV(event.getFOV() * (1.0F - zoomProgress * (1.0F - zoomFactor)));
    }

    /**
     * Draws a subtle radial vignette that intensifies with zoom level.
     */
    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.type != RenderGameOverlayEvent.ElementType.ALL) return;
        if (!isEnabled() || zoomProgress <= 0.0F) return;

        ScaledResolution res = event.resolution;
        drawVignette(res.getScaledWidth(), res.getScaledHeight(), zoomProgress);
    }

    private void startZoom() {
        zoomActive = true;
        if (originalSensitivity < 0.0F) {
            originalSensitivity = mc.gameSettings.mouseSensitivity;
        }
    }

    private void endZoom() {
        zoomActive = false;
        if (originalSensitivity >= 0.0F) {
            mc.gameSettings.mouseSensitivity = originalSensitivity;
            originalSensitivity = -1.0F;
        }
    }

    /**
     * Draws a radial black-to-transparent vignette using a triangle fan.
     * Recomputes screen-dependent metrics only when the resolution changes.
     */
    private void drawVignette(int screenWidth, int screenHeight, float progress) {
        if (screenWidth != lastScreenW || screenHeight != lastScreenH) {
            lastScreenW = screenWidth;
            lastScreenH = screenHeight;
            cachedMaxDist = Math.sqrt(screenWidth * screenWidth + screenHeight * screenHeight) * 1.22D;
        }

        int cx = screenWidth / 2;
        int cy = screenHeight / 2;
        double maxDist = cachedMaxDist;
        int segments = 64;
        int baseAlpha = (int) (160 * progress);

        GlStateManager.pushMatrix();
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.disableAlpha();

        WorldRenderer wr = Tessellator.getInstance().getWorldRenderer();
        wr.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_COLOR);

        // Center is transparent
        wr.pos(cx, cy, 0.0D).color(0, 0, 0, 0).endVertex();

        // Outer ring is dark
        for (int i = 0; i <= segments; i++) {
            double angle = i * 2.0D * Math.PI / segments;
            double px = cx + Math.cos(angle) * maxDist;
            double py = cy + Math.sin(angle) * maxDist;
            wr.pos(px, py, 0.0D).color(0, 0, 0, baseAlpha).endVertex();
        }

        Tessellator.getInstance().draw();

        GlStateManager.enableAlpha();
        GlStateManager.disableBlend();
        GlStateManager.enableTexture2D();
        GlStateManager.popMatrix();
    }
}
