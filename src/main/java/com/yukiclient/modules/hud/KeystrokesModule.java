package com.yukiclient.modules.hud;

import com.yukiclient.modules.Module;
import com.yukiclient.theme.YukiTheme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import com.yukiclient.util.ClickTracker;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

/**
 * Keystrokes Module - LunarClient Style.
 *
 * Replicates the LunarClient Keystrokes aesthetic:
 * - Unpressed keys: dark background with a crisp white border and white text.
 * - Pressed keys: bright cyan fill with dark text.
 * - WASD aligned with W centered above ASD.
 * - Space bar spans the full width of the ASD row.
 * - LMB / RMB each take half the row width and display live CPS counters.
 *
 * <p>Input polling and CPS counting happen on {@link TickEvent.ClientTickEvent}
 * so {@link #render()} only reads cached state and draws.</p>
 */
public class KeystrokesModule extends Module {
    private final Minecraft mc = Minecraft.getMinecraft();

    // Geometry
    private final int keySize = 26;
    private final int gap = 2;
    private final int totalRowWidth = 3 * keySize + 2 * gap;

    // CPS tracking
    private final ClickTracker leftTracker = new ClickTracker();
    private final ClickTracker rightTracker = new ClickTracker();

    // Cached per-tick key/button states
    private boolean forward, left, back, right, jump, lmb, rmb;
    private int leftCps;
    private int rightCps;

    // Cached formatted CPS strings
    private int lastLeftCps = -1;
    private String leftCpsText = "";
    private int lastRightCps = -1;
    private String rightCpsText = "";

    public KeystrokesModule() {
        super("Keystrokes", "Displays active movement keys.", Module.Category.HUD);
        this.x = 10;
        this.y = 100;
        this.width = totalRowWidth;
        this.height = 4 * keySize + 3 * gap;
    }

    /**
     * Polls input and updates CPS counters once per tick.
     */
    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) {
            return;
        }

        updateCps();

        forward = Keyboard.isKeyDown(mc.gameSettings.keyBindForward.getKeyCode());
        this.left = Keyboard.isKeyDown(mc.gameSettings.keyBindLeft.getKeyCode());
        back = Keyboard.isKeyDown(mc.gameSettings.keyBindBack.getKeyCode());
        this.right = Keyboard.isKeyDown(mc.gameSettings.keyBindRight.getKeyCode());
        jump = Keyboard.isKeyDown(mc.gameSettings.keyBindJump.getKeyCode());
        lmb = Mouse.isButtonDown(0);
        rmb = Mouse.isButtonDown(1);

        if (leftCps != lastLeftCps) {
            lastLeftCps = leftCps;
            leftCpsText = leftCps + " CPS";
        }
        if (rightCps != lastRightCps) {
            lastRightCps = rightCps;
            rightCpsText = rightCps + " CPS";
        }
    }

    /**
     * Updates rolling 1-second CPS counters for left and right clicks.
     */
    private void updateCps() {
        leftTracker.update(Mouse.isButtonDown(0));
        rightTracker.update(Mouse.isButtonDown(1));

        leftCps = leftTracker.getCps();
        rightCps = rightTracker.getCps();
    }

    @Override
    public void render() {
        int sx = this.x;
        int sy = this.y;

        // --- WASD ---
        // W centered above S
        drawKey("W", sx + keySize + gap, sy, forward, keySize, keySize);

        // ASD row
        drawKey("A", sx, sy + keySize + gap, left, keySize, keySize);
        drawKey("S", sx + keySize + gap, sy + keySize + gap, back, keySize, keySize);
        drawKey("D", sx + 2 * (keySize + gap), sy + keySize + gap, this.right, keySize, keySize);

        // --- Space bar (full row width) ---
        drawKey("_____", sx, sy + 2 * (keySize + gap), jump, totalRowWidth, keySize);

        // --- LMB / RMB (halves of the row) ---
        int mouseWidth = (totalRowWidth - gap) / 2;
        int mouseY = sy + 3 * (keySize + gap);

        drawMouseKey("LMB", leftCpsText, sx, mouseY, lmb, mouseWidth, keySize);
        drawMouseKey("RMB", rightCpsText, sx + mouseWidth + gap, mouseY, rmb, mouseWidth, keySize);
    }

    /**
     * Draws a frosted key cell. Idle keys show frost-blue text; pressed keys get
     * a warm sakura-tinted cell and snow-white text so they pop.
     */
    private void drawKey(String label, int x, int y, boolean pressed, int w, int h) {
        YukiTheme.drawFrostKey(x, y, w, h, pressed);

        int textCol = pressed ? YukiTheme.SNOW_WHITE : YukiTheme.FROST_BLUE;
        int tw = mc.fontRendererObj.getStringWidth(label);
        int tx = x + (w - tw) / 2;
        int ty = y + (h - 8) / 2;
        mc.fontRendererObj.drawString(label, tx, ty, textCol, false);
    }

    /**
     * Draws a mouse key box that also renders a CPS counter underneath the label.
     */
    private void drawMouseKey(String label, String cpsText, int x, int y, boolean pressed, int w, int h) {
        YukiTheme.drawFrostKey(x, y, w, h, pressed);

        int textCol = pressed ? YukiTheme.SNOW_WHITE : YukiTheme.FROST_BLUE;

        // Label (shifted up slightly to make room for CPS)
        int labelW = mc.fontRendererObj.getStringWidth(label);
        int labelX = x + (w - labelW) / 2;
        int labelY = y + (h - 16) / 2;
        mc.fontRendererObj.drawString(label, labelX, labelY, textCol, false);

        // CPS count, kept in frost-blue as a quiet secondary readout.
        int cpsW = mc.fontRendererObj.getStringWidth(cpsText);
        int cpsX = x + (w - cpsW) / 2;
        int cpsY = labelY + 8;
        mc.fontRendererObj.drawString(cpsText, cpsX, cpsY, pressed ? YukiTheme.SNOW_WHITE : YukiTheme.FROST_BLUE, false);
    }
}
