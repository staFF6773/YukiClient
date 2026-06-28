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
        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
        // Draw soft frost blue shadow behind the main text
        fr.drawString(text, x + 1, y + 1, FROST_BLUE, false);
        // Draw main crisp text on top
        fr.drawString(text, x, y, color, false);
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
}