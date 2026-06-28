package com.yukiclient.gui;

import com.yukiclient.config.ConfigManager;
import com.yukiclient.modules.Module;
import com.yukiclient.modules.ModuleManager;
import com.yukiclient.theme.YukiTheme;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Draggable HUD Editor Screen - LunarClient-inspired UX.
 *
 * Features:
 *  - Drag modules with the left mouse button.
 *  - Scroll while hovering a module to resize.
 *  - Grab any corner handle of a module and drag to resize from that corner.
 *  - Hold Shift while dragging to snap to a grid.
 *  - Smart alignment guides appear when edges line up with other modules.
 *  - Right-click a module for options (reset position, reset scale, toggle visibility).
 *  - Keyboard shortcuts: R = reset selected module, Delete = disable, G = grid, Esc = exit.
 *  - Global HUD scale is shown in the footer and persisted.
 *
 * Performance is improved by computing module bounds once per frame, caching the
 * hovered module, and avoiding repeated hit-tests.
 */
public class GuiEditScreen extends GuiScreen {
    private final ModuleManager moduleManager;
    private final ConfigManager configManager;

    // Movement
    private Module draggedModule = null;
    private int dragOffsetX = 0;
    private int dragOffsetY = 0;

    // Corner resize
    private Module resizedModule = null;
    private Corner resizeCorner = null;
    private float resizeStartScale;
    private int resizeStartMouseX, resizeStartMouseY;
    private int resizeStartLogicalW, resizeStartLogicalH;
    private int resizeAnchorX, resizeAnchorY;
    private boolean anchorIsRight, anchorIsBottom;
    private float resizeStartDist;

    private Module hoveredModule = null;

    // Cached hover state for badges so we don't format strings every frame.
    private Module lastBadgeModule = null;
    private String badgeText = "";
    private int badgeWidth = 0;

    // Grid / snap
    private static final int GRID_SIZE = 16;
    private static final int SNAP_THRESHOLD = 8;
    private boolean showGrid = true;

    // Right-click context menu
    private ContextMenu contextMenu = null;

    // Alignment guides
    private int guideX = Integer.MIN_VALUE;
    private int guideY = Integer.MIN_VALUE;

    // Handle visuals
    private static final int HANDLE_SIZE = 6;
    private static final int HANDLE_HALF = HANDLE_SIZE / 2;

    private enum Corner {
        TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT
    }

    public GuiEditScreen(ModuleManager manager, ConfigManager configManager) {
        this.moduleManager = manager;
        this.configManager = configManager;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        // Dim the world behind the editor.
        GuiScreen.drawRect(0, 0, this.width, this.height, YukiTheme.DARK_OVERLAY);

        // Grid behind everything.
        if (showGrid) {
            drawGrid();
        }

        drawTitle();

        // Defensive: close context menu if its module is no longer enabled.
        if (contextMenu != null && !contextMenu.module.isEnabled()) {
            contextMenu = null;
        }

        // Single pass: compute bounds, find hovered module / handle, draw borders & handles.
        ArrayList<Module> modules = moduleManager.getModulesView();
        int n = modules.size();

        hoveredModule = null;
        guideX = Integer.MIN_VALUE;
        guideY = Integer.MIN_VALUE;

        Corner hoveredCorner = null;

        for (int i = n - 1; i >= 0; i--) {
            Module mod = modules.get(i);
            if (!mod.isEnabled()) continue;

            int w = mod.getScaledWidth();
            int h = mod.getScaledHeight();

            // Check corner handles first (resize has priority over move).
            if (hoveredCorner == null) {
                hoveredCorner = getHoveredCorner(mod, mouseX, mouseY);
            }

            if (hoveredModule == null && hoveredCorner == null && mod.isMouseInside(mouseX, mouseY)) {
                hoveredModule = mod;
            }

            boolean active = (mod == draggedModule || mod == hoveredModule || mod == resizedModule);
            int borderColor = active ? YukiTheme.SAKURA_PINK : YukiTheme.EDIT_BORDER;

            GuiScreen.drawRect(mod.getX() - 1, mod.getY() - 1, mod.getX() + w + 1, mod.getY() + h + 1, borderColor);

            // Render the real module so the user sees the final look while editing.
            mod.renderScaled();
        }

        // Draw handles for the hovered/resized module on top of borders.
        Module handleModule = resizedModule != null ? resizedModule : hoveredModule;
        if (handleModule != null) {
            drawCornerHandles(handleModule, mouseX, mouseY);
        }

        // Draw alignment guides for the dragged module.
        if (draggedModule != null) {
            drawAlignmentGuides(draggedModule, modules);
        }

        // Module name / scale badge.
        if (hoveredModule != null || resizedModule != null) {
            drawModuleBadge(resizedModule != null ? resizedModule : hoveredModule);
        }

        // Context menu (rendered on top).
        if (contextMenu != null) {
            contextMenu.draw(mouseX, mouseY);
        }

        drawFooter();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void drawTitle() {
        String titleMain = "Yuki";
        String titleSub = " HUD Editor";
        int mainW = this.fontRendererObj.getStringWidth(titleMain);
        int subW = this.fontRendererObj.getStringWidth(titleSub);
        int titleX = (this.width - mainW - subW) / 2;
        int titleY = 12;
        this.fontRendererObj.drawString(titleMain, titleX, titleY, YukiTheme.SNOW_WHITE, false);
        this.fontRendererObj.drawString(titleSub, titleX + mainW, titleY, YukiTheme.FROST_BLUE, false);
    }

    private void drawFooter() {
        String hint;
        if (resizedModule != null) {
            hint = "Corner: Resize | Shift: Snap | Esc: Exit";
        } else {
            hint = "Drag: Move | Scroll: Resize | Corner: Resize | RMB: Options | G: Grid | Esc: Exit";
        }
        String global = String.format("Global Scale: %.2fx", Module.getGlobalScale());
        int hintW = this.fontRendererObj.getStringWidth(hint);
        int globalW = this.fontRendererObj.getStringWidth(global);

        this.fontRendererObj.drawString(hint, (this.width - hintW) / 2, this.height - 22, YukiTheme.FROST_BLUE, false);
        this.fontRendererObj.drawString(global, (this.width - globalW) / 2, this.height - 12, YukiTheme.SAKURA_PINK, false);
    }

    private void drawGrid() {
        int gridColor = 0x18FFFFFF;
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        for (int x = centerX % GRID_SIZE; x < this.width; x += GRID_SIZE) {
            GuiScreen.drawRect(x, 0, x + 1, this.height, gridColor);
        }
        for (int y = centerY % GRID_SIZE; y < this.height; y += GRID_SIZE) {
            GuiScreen.drawRect(0, y, this.width, y + 1, gridColor);
        }
    }

    private void drawAlignmentGuides(Module target, ArrayList<Module> modules) {
        int tx = target.getX();
        int ty = target.getY();
        int tw = target.getScaledWidth();
        int th = target.getScaledHeight();

        int[] targetEdges = { tx, tx + tw / 2, tx + tw, this.width / 2 };
        int[] targetTops = { ty, ty + th / 2, ty + th, this.height / 2 };

        boolean hasX = false;
        boolean hasY = false;

        for (Module mod : modules) {
            if (mod == target || !mod.isEnabled()) continue;

            int mx = mod.getX();
            int my = mod.getY();
            int mw = mod.getScaledWidth();
            int mh = mod.getScaledHeight();

            int[] edges = { mx, mx + mw / 2, mx + mw };
            int[] tops = { my, my + mh / 2, my + mh };

            for (int te : targetEdges) {
                for (int e : edges) {
                    if (Math.abs(te - e) <= SNAP_THRESHOLD && !hasX) {
                        guideX = e;
                        hasX = true;
                    }
                }
            }

            for (int tt : targetTops) {
                for (int t : tops) {
                    if (Math.abs(tt - t) <= SNAP_THRESHOLD && !hasY) {
                        guideY = t;
                        hasY = true;
                    }
                }
            }
        }

        if (guideX != Integer.MIN_VALUE) {
            GuiScreen.drawRect(guideX, 0, guideX + 1, this.height, YukiTheme.SAKURA_PINK & 0x80FFFFFF);
        }
        if (guideY != Integer.MIN_VALUE) {
            GuiScreen.drawRect(0, guideY, this.width, guideY + 1, YukiTheme.SAKURA_PINK & 0x80FFFFFF);
        }
    }

    private Corner getHoveredCorner(Module mod, int mouseX, int mouseY) {
        int w = mod.getScaledWidth();
        int h = mod.getScaledHeight();
        int x = mod.getX();
        int y = mod.getY();

        if (isNearCorner(mouseX, mouseY, x, y)) return Corner.TOP_LEFT;
        if (isNearCorner(mouseX, mouseY, x + w, y)) return Corner.TOP_RIGHT;
        if (isNearCorner(mouseX, mouseY, x, y + h)) return Corner.BOTTOM_LEFT;
        if (isNearCorner(mouseX, mouseY, x + w, y + h)) return Corner.BOTTOM_RIGHT;
        return null;
    }

    private boolean isNearCorner(int mx, int my, int cx, int cy) {
        return Math.abs(mx - cx) <= HANDLE_HALF && Math.abs(my - cy) <= HANDLE_HALF;
    }

    private void drawCornerHandles(Module mod, int mouseX, int mouseY) {
        int w = mod.getScaledWidth();
        int h = mod.getScaledHeight();
        int x = mod.getX();
        int y = mod.getY();

        Corner hoveredCorner = getHoveredCorner(mod, mouseX, mouseY);

        drawHandle(x, y, hoveredCorner == Corner.TOP_LEFT);
        drawHandle(x + w, y, hoveredCorner == Corner.TOP_RIGHT);
        drawHandle(x, y + h, hoveredCorner == Corner.BOTTOM_LEFT);
        drawHandle(x + w, y + h, hoveredCorner == Corner.BOTTOM_RIGHT);
    }

    private void drawHandle(int cx, int cy, boolean hovered) {
        int color = hovered ? YukiTheme.SAKURA_PINK : YukiTheme.SNOW_WHITE;
        GuiScreen.drawRect(cx - HANDLE_HALF, cy - HANDLE_HALF, cx + HANDLE_HALF, cy + HANDLE_HALF, color);
    }

    private void drawModuleBadge(Module mod) {
        if (mod != lastBadgeModule) {
            lastBadgeModule = mod;
            badgeText = String.format("%s  %.2fx (eff. %.2fx)", mod.getName(), mod.getScale(), mod.getEffectiveScale());
            badgeWidth = this.fontRendererObj.getStringWidth(badgeText);
        }

        int w = mod.getScaledWidth();

        int bx = mod.getX() + w - badgeWidth - 5;
        int by = mod.getY() - 13;
        if (by < 2) {
            by = mod.getY() + mod.getScaledHeight() + 4;
        }

        GuiScreen.drawRect(bx - 4, by - 2, bx + badgeWidth + 4, by + 10, YukiTheme.DARK_SLATE);
        GuiScreen.drawRect(bx - 4, by - 2, bx - 2, by + 10, YukiTheme.SAKURA_PINK);
        this.fontRendererObj.drawString(badgeText, bx, by, YukiTheme.SNOW_WHITE, false);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        // Close any open context menu first.
        if (contextMenu != null) {
            if (contextMenu.click(mouseX, mouseY)) {
                contextMenu = null;
                return;
            }
            contextMenu = null;
        }

        if (mouseButton == 0) {
            // Check for a corner resize handle before starting a drag.
            Module handleMod = getModuleWithHandleAt(mouseX, mouseY);
            if (handleMod != null) {
                startResize(handleMod, getHoveredCorner(handleMod, mouseX, mouseY), mouseX, mouseY);
                return;
            }

            Module mod = getModuleAt(mouseX, mouseY);
            if (mod != null) {
                draggedModule = mod;
                dragOffsetX = mouseX - mod.getX();
                dragOffsetY = mouseY - mod.getY();
            }
        } else if (mouseButton == 1) {
            Module mod = getModuleAt(mouseX, mouseY);
            if (mod != null) {
                openContextMenu(mod, mouseX, mouseY);
            }
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        if (resizedModule != null && clickedMouseButton == 0) {
            updateResize(mouseX, mouseY);
            return;
        }

        if (draggedModule != null && clickedMouseButton == 0) {
            int rawX = mouseX - dragOffsetX;
            int rawY = mouseY - dragOffsetY;

            if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
                rawX = roundToGrid(rawX);
                rawY = roundToGrid(rawY);
            }

            draggedModule.setX(clampX(rawX, draggedModule.getScaledWidth()));
            draggedModule.setY(clampY(rawY, draggedModule.getScaledHeight()));
        }
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        draggedModule = null;
        resizedModule = null;
        resizeCorner = null;
        super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    public void handleMouseInput() throws IOException {
        // Read wheel and coordinates before super processes the event, so we don't miss values.
        int wheel = Mouse.getEventDWheel();
        boolean ctrl = Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL);
        boolean shift = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);

        super.handleMouseInput();

        if (wheel == 0) return;

        int mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth;
        int mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;

        // Global HUD scale: Ctrl + Shift + scroll anywhere.
        if (ctrl && shift) {
            float dir = wheel > 0 ? Module.COARSE_STEP : -Module.COARSE_STEP;
            Module.setGlobalScale(Module.getGlobalScale() + dir);
            return;
        }

        // Per-module scale only when hovering a module.
        Module target = getModuleAt(mouseX, mouseY);
        if (target == null) return;

        float step = Module.SCALE_STEP;
        if (shift) {
            step = Module.COARSE_STEP;
        }

        float oldScale = target.getScale();
        float newScale = Math.max(Module.MIN_SCALE, Math.min(Module.MAX_SCALE, oldScale + (wheel > 0 ? step : -step)));
        if (newScale == oldScale) return;

        // Cursor-anchored scaling.
        float global = Module.getGlobalScale();
        float localX = (mouseX - target.getX()) / (oldScale * global);
        float localY = (mouseY - target.getY()) / (oldScale * global);
        int newX = Math.round(mouseX - localX * newScale * global);
        int newY = Math.round(mouseY - localY * newScale * global);

        target.setScale(newScale);
        target.setX(clampX(newX, target.getScaledWidth()));
        target.setY(clampY(newY, target.getScaledHeight()));
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == Keyboard.KEY_G) {
            showGrid = !showGrid;
            return;
        }

        if (keyCode == Keyboard.KEY_R) {
            Module target = draggedModule != null ? draggedModule : hoveredModule;
            if (target != null) {
                resetModule(target);
            }
            return;
        }

        if (keyCode == Keyboard.KEY_DELETE) {
            Module target = draggedModule != null ? draggedModule : hoveredModule;
            if (target != null) {
                target.setEnabled(false);
            }
            return;
        }

        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        if (configManager != null) {
            configManager.save();
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    private Module getModuleWithHandleAt(int mouseX, int mouseY) {
        ArrayList<Module> mods = moduleManager.getModulesView();
        for (int i = mods.size() - 1; i >= 0; i--) {
            Module mod = mods.get(i);
            if (!mod.isEnabled()) continue;
            if (getHoveredCorner(mod, mouseX, mouseY) != null) {
                return mod;
            }
        }
        return null;
    }

    /**
     * Returns the topmost enabled module at the given coordinates. Bounds already
     * account for each module's effective scale.
     */
    private Module getModuleAt(int mouseX, int mouseY) {
        ArrayList<Module> mods = moduleManager.getModulesView();
        for (int i = mods.size() - 1; i >= 0; i--) {
            Module mod = mods.get(i);
            if (mod.isEnabled() && mod.isMouseInside(mouseX, mouseY)) {
                return mod;
            }
        }
        return null;
    }

    private void openContextMenu(Module mod, int mouseX, int mouseY) {
        contextMenu = new ContextMenu(mod, mouseX, mouseY);
    }

    private void resetModule(Module mod) {
        mod.setScale(1.0f);
        int cx = Math.max(0, Math.min(this.width - mod.getScaledWidth(), mod.getX()));
        int cy = Math.max(0, Math.min(this.height - mod.getScaledHeight(), mod.getY()));
        mod.setX(cx);
        mod.setY(cy);
    }

    private void startResize(Module mod, Corner corner, int mouseX, int mouseY) {
        resizedModule = mod;
        resizeCorner = corner;
        resizeStartScale = mod.getScale();
        resizeStartMouseX = mouseX;
        resizeStartMouseY = mouseY;
        resizeStartLogicalW = mod.getWidth();
        resizeStartLogicalH = mod.getHeight();

        int w = mod.getScaledWidth();
        int h = mod.getScaledHeight();
        int x = mod.getX();
        int y = mod.getY();

        // Determine the anchor corner that stays fixed.
        switch (corner) {
            case TOP_LEFT:
                resizeAnchorX = x + w;
                resizeAnchorY = y + h;
                anchorIsRight = true;
                anchorIsBottom = true;
                break;
            case TOP_RIGHT:
                resizeAnchorX = x;
                resizeAnchorY = y + h;
                anchorIsRight = false;
                anchorIsBottom = true;
                break;
            case BOTTOM_LEFT:
                resizeAnchorX = x + w;
                resizeAnchorY = y;
                anchorIsRight = true;
                anchorIsBottom = false;
                break;
            case BOTTOM_RIGHT:
                resizeAnchorX = x;
                resizeAnchorY = y;
                anchorIsRight = false;
                anchorIsBottom = false;
                break;
        }

        resizeStartDist = distance(mouseX, mouseY, resizeAnchorX, resizeAnchorY);
    }

    private void updateResize(int mouseX, int mouseY) {
        if (resizeStartDist == 0) return;

        float currentDist = distance(mouseX, mouseY, resizeAnchorX, resizeAnchorY);
        float ratio = currentDist / resizeStartDist;
        float newScale = Math.max(Module.MIN_SCALE, Math.min(Module.MAX_SCALE, resizeStartScale * ratio));

        // Optional: snap scale to clean steps if Shift is held.
        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            newScale = Math.round(newScale / Module.SCALE_STEP) * Module.SCALE_STEP;
            newScale = Math.max(Module.MIN_SCALE, Math.min(Module.MAX_SCALE, newScale));
        }

        float global = Module.getGlobalScale();
        int newEffW = Math.round(resizeStartLogicalW * newScale * global);
        int newEffH = Math.round(resizeStartLogicalH * newScale * global);

        int newX = anchorIsRight ? resizeAnchorX - newEffW : resizeAnchorX;
        int newY = anchorIsBottom ? resizeAnchorY - newEffH : resizeAnchorY;

        resizedModule.setScale(newScale);
        resizedModule.setX(clampX(newX, newEffW));
        resizedModule.setY(clampY(newY, newEffH));
    }

    private float distance(int x1, int y1, int x2, int y2) {
        int dx = x1 - x2;
        int dy = y1 - y2;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    private int roundToGrid(int value) {
        return Math.round(value / (float) GRID_SIZE) * GRID_SIZE;
    }

    private int clampX(int x, int width) {
        int margin = Math.min(16, width);
        return Math.max(-width + margin, Math.min(this.width - margin, x));
    }

    private int clampY(int y, int height) {
        int margin = Math.min(16, height);
        return Math.max(-height + margin, Math.min(this.height - margin, y));
    }

    /**
     * Simple right-click context menu rendered with Minecraft's immediate-mode rectangles.
     */
    private class ContextMenu {
        private final Module module;
        private final int x;
        private final int y;
        private final int itemHeight = 12;
        private final int width;
        private final String[] labels = { "Reset Position", "Reset Scale", "Reset All", "Toggle Visibility" };

        ContextMenu(Module module, int x, int y) {
            this.module = module;
            int maxW = 0;
            for (String label : labels) {
                int w = fontRendererObj.getStringWidth(label);
                if (w > maxW) maxW = w;
            }
            this.width = maxW + 10;
            this.x = x;
            this.y = y;
        }

        void draw(int mouseX, int mouseY) {
            int h = labels.length * itemHeight + 4;
            GuiScreen.drawRect(x, y, x + width, y + h, YukiTheme.PANEL_BG);
            GuiScreen.drawRect(x, y, x + width, y + 1, YukiTheme.FROST_BLUE);
            GuiScreen.drawRect(x, y + h - 1, x + width, y + h, YukiTheme.FROST_BLUE);

            for (int i = 0; i < labels.length; i++) {
                int iy = y + 2 + i * itemHeight;
                boolean hovered = mouseX >= x && mouseX <= x + width && mouseY >= iy && mouseY < iy + itemHeight;
                if (hovered) {
                    GuiScreen.drawRect(x + 1, iy, x + width - 1, iy + itemHeight, YukiTheme.HOVER_TINT);
                }
                fontRendererObj.drawString(labels[i], x + 5, iy + 2, YukiTheme.SNOW_WHITE, false);
            }
        }

        /**
         * Returns true if the click was handled by this menu.
         */
        boolean click(int mouseX, int mouseY) {
            if (mouseX < x || mouseX > x + width || mouseY < y) return false;
            int relY = mouseY - y - 2;
            if (relY < 0 || relY >= labels.length * itemHeight) return false;
            int index = relY / itemHeight;

            switch (index) {
                case 0:
                    int cx = Math.max(0, Math.min(GuiEditScreen.this.width - module.getScaledWidth(), module.getX()));
                    int cy = Math.max(0, Math.min(GuiEditScreen.this.height - module.getScaledHeight(), module.getY()));
                    module.setX(cx);
                    module.setY(cy);
                    break;
                case 1:
                    module.setScale(1.0f);
                    break;
                case 2:
                    resetModule(module);
                    break;
                case 3:
                    module.toggle();
                    break;
            }
            return true;
        }
    }
}
