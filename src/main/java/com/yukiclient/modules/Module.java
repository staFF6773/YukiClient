package com.yukiclient.modules;

import net.minecraft.client.renderer.GlStateManager;

/**
 * Abstract base class for all YukiClient HUD modules.
 * Manages common properties such as positioning, dimensions, and enabled state.
 */
public abstract class Module {

    /** Minimum HUD scale allowed in the editor. */
    public static final float MIN_SCALE = 0.25f;
    /** Maximum HUD scale allowed in the editor. */
    public static final float MAX_SCALE = 2.0f;
    /** Fine step used when scaling an individual module. */
    public static final float SCALE_STEP = 0.05f;
    /** Coarse step used with Shift + scroll. */
    public static final float COARSE_STEP = 0.1f;

    /** Minimum global HUD scale multiplier. */
    public static final float MIN_GLOBAL_SCALE = 0.25f;
    /** Maximum global HUD scale multiplier. */
    public static final float MAX_GLOBAL_SCALE = 3.0f;

    /** Global multiplier applied to every HUD module. */
    private static float globalScale = 1.0f;

    /**
     * Logical grouping for modules, used to organize the ClickGUI into sections.
     */
    public enum Category {
        HUD,
        BEHAVIOR
    }

    protected final String name;
    protected final String description;
    protected final Category category;
    protected boolean enabled;
    protected int x, y;
    protected int width, height;
    protected float scale = 1.0f;

    public Module(String name, String description) {
        this(name, description, Category.BEHAVIOR);
    }

    public Module(String name, String description, Category category) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.enabled = true; // Modules are enabled by default
    }

    /* === Accessors === */

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Category getCategory() {
        return category;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Toggles the module on/off.
     */
    public void toggle() {
        this.enabled = !this.enabled;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public float getScale() {
        return scale;
    }

    /**
     * Sets the render scale, clamped to [{@link #MIN_SCALE}, {@link #MAX_SCALE}].
     */
    public void setScale(float scale) {
        this.scale = Math.max(MIN_SCALE, Math.min(MAX_SCALE, scale));
    }

    /**
     * Returns the global HUD scale multiplier applied to every module.
     */
    public static float getGlobalScale() {
        return globalScale;
    }

    /**
     * Sets the global HUD scale multiplier. Values are clamped to a safe range.
     */
    public static void setGlobalScale(float scale) {
        globalScale = Math.max(MIN_GLOBAL_SCALE, Math.min(MAX_GLOBAL_SCALE, scale));
    }

    /**
     * Effective scale used for rendering and hit testing = per-module scale * global scale.
     */
    public float getEffectiveScale() {
        return scale * globalScale;
    }

    /**
     * Returns the logical width scaled by the effective scale.
     */
    public int getScaledWidth() {
        return Math.round(width * getEffectiveScale());
    }

    /**
     * Returns the logical height scaled by the effective scale.
     */
    public int getScaledHeight() {
        return Math.round(height * getEffectiveScale());
    }

    /**
     * Renders this module with its effective scale applied. Scaling is performed
     * around the module's top-left corner so its position stays anchored.
     * At effective scale 1.0 this is equivalent to {@link #render()}.
     */
    public void renderScaled() {
        float effective = getEffectiveScale();
        if (effective == 1.0f) {
            render();
            return;
        }
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, 0);
        GlStateManager.scale(effective, effective, 1.0f);
        GlStateManager.translate(-x, -y, 0);
        render();
        GlStateManager.popMatrix();
    }

    /**
     * Checks if the given mouse coordinates are within this module's scaled bounds.
     * Used for the draggable GUI editor.
     */
    public boolean isMouseInside(int mouseX, int mouseY) {
        int sw = getScaledWidth();
        int sh = getScaledHeight();
        return mouseX >= x && mouseY >= y
            && mouseX <= x + sw && mouseY <= y + sh;
    }

    /**
     * Renders a lightweight placeholder rectangle for the editor. Avoids the cost
     * of the module's real render path while still showing position and size.
     */
    public void renderEditorPlaceholder() {
        net.minecraft.client.gui.GuiScreen.drawRect(x, y, x + getScaledWidth(), y + getScaledHeight(), 0x40B0E0E6);
    }

    /**
     * Render method called every frame for the HUD.
     * Each module implements its own drawing logic here.
     */
    public abstract void render();
}