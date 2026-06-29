package com.yukiclient.theme;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;

/**
 * Centralized theme definition for the Japanese Snow aesthetic.
 * All HUD modules reference this class to maintain visual consistency.
 */
public class YukiTheme {

    /* === Japanese Snow Palette Hex Codes === */

    /**
     * Clean Snow White: Pure white for primary text.
     * High contrast against translucent backgrounds.
     */
    public static final int SNOW_WHITE = 0xFFFFFF;

    /**
     * Soft Frost Blue: Used for shadows, accents, and secondary highlights.
     * Gives text a subtle "cold" glow effect.
     */
    public static final int FROST_BLUE = 0xB0E0E6;

    /**
     * Sakura Pink: Used sparingly for active states or hover highlights.
     * Introduces a touch of warmth against the cold winter theme.
     */
    public static final int SAKURA_PINK = 0xFFB7C5;

    /**
     * Slate Gray: Used for subtle borders or inactive elements.
     */
    public static final int SLATE_GRAY = 0x708090;

    /**
     * Dark Slate: Used for deep shadow or translucent background tints.
     */
    public static final int DARK_SLATE = 0x2F4F4F;

    /**
     * Translucent Dark Overlay: Used for the background of the GUI Edit Screen.
     * Deep blue-grey to make the snow-white modules pop.
     * Format: AARRGGBB
     */
    public static final int DARK_OVERLAY = 0xDD1A2530;

    /* === ClickGUI "Frost" Panel Palette === */

    /**
     * Panel Background: A calm, deep charcoal-blue for the ClickGUI card.
     * More neutral than DARK_OVERLAY to feel modern and quiet.
     * Format: AARRGGBB
     */
    public static final int PANEL_BG = 0xE8181F28;

    /**
     * Hover Tint: Soft frost blue at low opacity, applied to a row on hover.
     * Replaces hard border outlines with a gentle background cue.
     */
    public static final int HOVER_TINT = 0x28B0E0E6;

    /**
     * Enabled Tint: Sakura pink at very low opacity, applied to enabled rows.
     * Gives active modules a subtle warm wash against the cold theme.
     */
    public static final int ENABLED_TINT = 0x18FFB7C5;

    /**
     * Divider: Translucent frost blue used for subtle separator lines
     * (e.g. the footer divider beneath the module list).
     */
    public static final int DIVIDER = 0x40B0E0E6;

    /**
     * Edit Border: Translucent frost blue outline drawn around HUD modules
     * in the editor when they are idle. Switches to Sakura Pink when active.
     */
    public static final int EDIT_BORDER = 0x80B0E0E6;

    /* === LunarClient-Inspired HUD Box Palette === */

    /**
     * Bright white border used on LunarClient-style HUD boxes.
     */
    public static final int LUNAR_BORDER = 0xFFFFFFFF;

    /**
     * Dark gray background used on LunarClient-style HUD boxes.
     */
    public static final int LUNAR_BG = 0xFF222222;

    /**
     * Bright cyan accent used when a LunarClient-style box is active/pressed.
     */
    public static final int LUNAR_ACCENT = 0xFF00BFFF;

    /**
     * Dark text color used on top of the bright cyan accent.
     */
    public static final int LUNAR_DARK_TEXT = 0xFF111111;

    /* === Custom Text Rendering Engine === */

    /**
     * Renders text with a soft frost-blue drop shadow.
     * The shadow is offset by +1, +1 to create a subtle "cold" depth.
     *
     * @param text  The string to render.
     * @param x     X screen coordinate.
     * @param y     Y screen coordinate.
     * @param color The main text color (typically SNOW_WHITE).
     */
    public static void drawStringWithFrostShadow(String text, int x, int y, int color) {
        // Use the vanilla crisp drop shadow for clean, readable text. The old
        // frost-blue +1/+1 offset rendered as an opaque ghost that made the
        // text look doubled and blurry.
        Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow(text, (float) x, (float) y, color);
    }

    /* === LunarClient-Style Box Renderer === */

    /**
     * Draws a dark LunarClient-style HUD box with a crisp white border.
     *
     * @param x      Top-left X coordinate.
     * @param y      Top-left Y coordinate.
     * @param width  Box width.
     * @param height Box height.
     */
    public static void drawLunarBox(int x, int y, int width, int height) {
        drawLunarBox(x, y, width, height, false);
    }

    /**
     * Draws a LunarClient-style HUD box with a crisp white border.
     * When pressed is true, the background fills with the bright cyan accent
     * and prepared for dark text.
     *
     * @param x      Top-left X coordinate.
     * @param y      Top-left Y coordinate.
     * @param width  Box width.
     * @param height Box height.
     * @param pressed Whether the box is in an active/pressed state.
     */
    public static void drawLunarBox(int x, int y, int width, int height, boolean pressed) {
        int bgColor = pressed ? LUNAR_ACCENT : LUNAR_BG;

        // Outer border
        Gui.drawRect(x, y, x + width, y + height, LUNAR_BORDER);
        // Inner background (inset by 1px)
        Gui.drawRect(x + 1, y + 1, x + width - 1, y + height - 1, bgColor);
    }

    /* === Japanese Snow HUD Palette (frosted glass) === */

    /** Legacy flat backgrounds (kept for compatibility). Format: AARRGGBB. */
    public static final int FROST_PANEL_BG = 0xB0141C28;
    public static final int FROST_PANEL_BG_ACTIVE = 0xC0301F33;
    /** Soft frost-blue hairline border. */
    public static final int FROST_HAIRLINE = 0x55B0E0E6;
    /** Soft sakura hairline border for active/pressed elements. */
    public static final int SAKURA_HAIRLINE = 0x99FFB7C5;
    /** Frost-blue accent bar drawn on the left edge of a panel (the Yuki signature). */
    public static final int FROST_ACCENT_BAR = 0xFFB0E0E6;
    /** Sakura accent bar drawn on the left edge of an active panel. */
    public static final int SAKURA_ACCENT_BAR = 0xFFFFB7C5;

    /* Vertical-gradient stops for the frosted panels (top -> bottom). */
    /** Idle panel gradient: lighter frost indigo at the top... */
    public static final int FROST_GRAD_TOP = 0xB81E2A40;
    /** ...fading into a deeper indigo at the bottom. */
    public static final int FROST_GRAD_BOTTOM = 0xC00C111C;
    /** Active panel gradient: warm plum top... */
    public static final int FROST_GRAD_TOP_ACTIVE = 0xC2402A48;
    /** ...into a deeper plum bottom. */
    public static final int FROST_GRAD_BOTTOM_ACTIVE = 0xC81E1124;

    /* Rounded gradient stops for the ClickGUI card. */
    public static final int PANEL_CARD_BORDER = 0x66B0E0E6;
    public static final int PANEL_GRAD_TOP = 0xF21E2636;
    public static final int PANEL_GRAD_BOTTOM = 0xF2121826;

    /** Default corner radius for HUD panels, in pixels. */
    private static final int PANEL_RADIUS = 3;
    /** Corner radius for the smaller keystroke cells. */
    private static final int KEY_RADIUS = 2;

    /**
     * Draws a 1px hollow rectangle (border only) in the given color.
     */
    public static void drawHollowRect(int x, int y, int width, int height, int color) {
        Gui.drawRect(x, y, x + width, y + 1, color);                          // top
        Gui.drawRect(x, y + height - 1, x + width, y + height, color);        // bottom
        Gui.drawRect(x, y + 1, x + 1, y + height - 1, color);                 // left
        Gui.drawRect(x + width - 1, y + 1, x + width, y + height - 1, color); // right
    }

    /**
     * Linearly interpolates between two ARGB colors. {@code t} is clamped 0..1.
     */
    private static int lerpColor(int a, int b, float t) {
        if (t < 0f) t = 0f;
        if (t > 1f) t = 1f;
        int aa = (a >>> 24) & 0xFF, ar = (a >>> 16) & 0xFF, ag = (a >>> 8) & 0xFF, ab = a & 0xFF;
        int ba = (b >>> 24) & 0xFF, br = (b >>> 16) & 0xFF, bg = (b >>> 8) & 0xFF, bb = b & 0xFF;
        int ca = (int) (aa + (ba - aa) * t);
        int cr = (int) (ar + (br - ar) * t);
        int cg = (int) (ag + (bg - ag) * t);
        int cb = (int) (ab + (bb - ab) * t);
        return (ca << 24) | (cr << 16) | (cg << 8) | cb;
    }

    /**
     * Fills a rounded rectangle with a vertical gradient. Corners are rounded by
     * insetting each scanline near the top/bottom edges (a clean 45-degree bevel
     * that reads as a soft rounded corner at small radii). Drawn one 1px row at a
     * time, which is cheap for the small panels used here.
     */
    public static void fillRoundedGradient(int x, int y, int width, int height, int radius,
                                           int topColor, int bottomColor) {
        if (width <= 0 || height <= 0) {
            return;
        }
        for (int row = 0; row < height; row++) {
            int edge = Math.min(row, height - 1 - row);
            int inset = Math.max(0, radius - edge);
            if (inset * 2 >= width) {
                inset = 0; // guard against over-insetting tiny boxes
            }
            float t = height <= 1 ? 0f : (float) row / (height - 1);
            int color = lerpColor(topColor, bottomColor, t);
            Gui.drawRect(x + inset, y + row, x + width - inset, y + row + 1, color);
        }
    }

    /**
     * Draws a frosted-glass HUD panel in the Japanese Snow style: a rounded card
     * with a soft vertical indigo gradient, a frost-blue hairline border and a
     * thin accent bar on the left edge (the Yuki signature).
     */
    public static void drawFrostPanel(int x, int y, int width, int height) {
        drawFrostPanel(x, y, width, height, false);
    }

    /**
     * Draws a frosted-glass panel. When {@code active} is true the panel uses a
     * warmer plum gradient with a sakura-pink accent + border to highlight it.
     */
    public static void drawFrostPanel(int x, int y, int width, int height, boolean active) {
        int border = active ? SAKURA_HAIRLINE : FROST_HAIRLINE;
        int top = active ? FROST_GRAD_TOP_ACTIVE : FROST_GRAD_TOP;
        int bottom = active ? FROST_GRAD_BOTTOM_ACTIVE : FROST_GRAD_BOTTOM;

        // Rounded border layer, then the gradient fill inset by 1px.
        fillRoundedGradient(x, y, width, height, PANEL_RADIUS, border, border);
        fillRoundedGradient(x + 1, y + 1, width - 2, height - 2, Math.max(0, PANEL_RADIUS - 1), top, bottom);

        // Left accent bar, inset vertically so it follows the rounded corners.
        int accent = active ? SAKURA_ACCENT_BAR : FROST_ACCENT_BAR;
        Gui.drawRect(x + 1, y + 2, x + 3, y + height - 2, accent);
    }

    /**
     * Draws a small rounded frosted key cell for the Keystrokes display. Unlike a
     * panel it has no left accent bar so a grid of keys stays clean and even.
     */
    public static void drawFrostKey(int x, int y, int width, int height, boolean pressed) {
        int border = pressed ? SAKURA_HAIRLINE : FROST_HAIRLINE;
        int top = pressed ? FROST_GRAD_TOP_ACTIVE : FROST_GRAD_TOP;
        int bottom = pressed ? FROST_GRAD_BOTTOM_ACTIVE : FROST_GRAD_BOTTOM;

        fillRoundedGradient(x, y, width, height, KEY_RADIUS, border, border);
        fillRoundedGradient(x + 1, y + 1, width - 2, height - 2, Math.max(0, KEY_RADIUS - 1), top, bottom);
    }

    /**
     * Draws the large rounded ClickGUI card: a frost-blue hairline border around
     * a soft vertical gradient, matching the HUD panels for a cohesive look.
     */
    public static void drawPanelCard(int x, int y, int width, int height) {
        int radius = 6;
        fillRoundedGradient(x, y, width, height, radius, PANEL_CARD_BORDER, PANEL_CARD_BORDER);
        fillRoundedGradient(x + 1, y + 1, width - 2, height - 2, radius - 1, PANEL_GRAD_TOP, PANEL_GRAD_BOTTOM);
    }
}