package com.yukiclient.modules.hud;

import com.yukiclient.modules.Module;
import com.yukiclient.theme.YukiTheme;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import com.yukiclient.util.ClickTracker;
import org.lwjgl.input.Mouse;

/**
 * CPS (Clicks Per Second) Module - LunarClient Style.
 *
 * Displays left and right click rates in two side-by-side dark boxes.
 * Each box has a crisp white border and highlights in bright cyan while
 * the corresponding mouse button is held down.
 *
 * <p>Input handling and CPS bookkeeping run on {@link TickEvent.ClientTickEvent}
 * so the render path only performs drawing.</p>
 */
public class CpsModule extends Module {
    private final Minecraft mc = Minecraft.getMinecraft();

    // Box geometry
    private final int boxWidth = 42;
    private final int boxHeight = 30;
    private final int gap = 2;

    // CPS tracking
    private final ClickTracker leftTracker = new ClickTracker();
    private final ClickTracker rightTracker = new ClickTracker();

    // Cached per-tick state used during rendering
    private boolean leftDown;
    private boolean rightDown;
    private int leftCps;
    private int rightCps;

    // Cached formatted strings to avoid per-frame allocations
    private int lastLeftCps = -1;
    private String leftCpsText = "";
    private int lastRightCps = -1;
    private String rightCpsText = "";

    public CpsModule() {
        super("CPS", "Displays left and right clicks per second.", Module.Category.HUD);
        // Positioned just below the FPS module by default
        this.x = 10;
        this.y = 30;
        this.width = boxWidth * 2 + gap;
        this.height = boxHeight;
    }

    /**
     * Records clicks and prunes expired entries once per tick.
     * This keeps the render path allocation-free and off the input hot path.
     */
    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) {
            return;
        }

        leftDown = Mouse.isButtonDown(0);
        rightDown = Mouse.isButtonDown(1);

        leftTracker.update(leftDown);
        rightTracker.update(rightDown);

        leftCps = leftTracker.getCps();
        rightCps = rightTracker.getCps();

        // Refresh cached strings only when values change.
        if (leftCps != lastLeftCps) {
            lastLeftCps = leftCps;
            leftCpsText = String.valueOf(leftCps);
        }
        if (rightCps != lastRightCps) {
            lastRightCps = rightCps;
            rightCpsText = String.valueOf(rightCps);
        }
    }

    @Override
    public void render() {
        // --- Left Box ---
        YukiTheme.drawFrostPanel(this.x, this.y, boxWidth, boxHeight, leftDown);
        drawCpsText("LMB", leftCpsText, this.x, this.y, boxWidth, boxHeight, leftDown);

        // --- Right Box ---
        int rightX = this.x + boxWidth + gap;
        YukiTheme.drawFrostPanel(rightX, this.y, boxWidth, boxHeight, rightDown);
        drawCpsText("RMB", rightCpsText, rightX, this.y, boxWidth, boxHeight, rightDown);

        // Update dimensions for the GUI editor
        this.width = boxWidth * 2 + gap;
        this.height = boxHeight;
    }

    /**
     * Draws the label and CPS value inside a box, centered as a two-line block.
     */
    private void drawCpsText(String label, String cpsText, int x, int y, int w, int h, boolean pressed) {
        // Label dims to frost-blue when idle and lights up snow-white when pressed.
        int labelColor = pressed ? YukiTheme.SNOW_WHITE : YukiTheme.FROST_BLUE;
        int valueColor = YukiTheme.SNOW_WHITE;

        int labelWidth = mc.fontRendererObj.getStringWidth(label);
        int labelX = x + 2 + (w - 2 - labelWidth) / 2;
        int labelY = y + (h - 16) / 2;
        mc.fontRendererObj.drawString(label, labelX, labelY, labelColor, false);

        int valueWidth = mc.fontRendererObj.getStringWidth(cpsText);
        int valueX = x + 2 + (w - 2 - valueWidth) / 2;
        int valueY = labelY + 8;
        mc.fontRendererObj.drawString(cpsText, valueX, valueY, valueColor, false);
    }
}
