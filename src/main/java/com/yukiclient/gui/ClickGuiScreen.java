package com.yukiclient.gui;

import com.yukiclient.YukiClient;
import com.yukiclient.config.ConfigManager;
import com.yukiclient.modules.Module;
import com.yukiclient.modules.ModuleManager;
import com.yukiclient.theme.YukiTheme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * ClickGUI Screen for toggling YukiClient modules.
 *
 * Modern, minimalist "Frost" layout:
 * - A single centered card on a dimmed background.
 * - Modules grouped under category headers (HUD / MODULES).
 * - Borderless rows that use background tints and a sakura-pink accent bar
 *   for enabled state instead of heavy outlines.
 * - A footer line that shows the hovered module's description.
 */
public class ClickGuiScreen extends GuiScreen {
    private final ModuleManager moduleManager;
    private final ConfigManager configManager;

    /* === Layout constants === */
    private static final int PANEL_WIDTH = 240;
    private static final int PADDING = 14;
    private static final int ROW_HEIGHT = 22;
    private static final int TITLE_BLOCK = 28;
    private static final int ACCENT = 1;
    private static final int SECTION_GAP = 8;
    private static final int HEADER_BLOCK = 14;
    private static final int FOOTER_GAP = 8;
    private static final int FOOTER_TEXT_GAP = 6;
    private static final int FONT_HEIGHT = 8;
    private static final int BOTTOM_PAD = 10;

    private int panelHeight;
    private int panelX;
    private int panelY;

    private List<Module> hudModules;
    private List<Module> behaviorModules;

    private final List<Row> rows = new ArrayList<Row>();
    private final List<Header> headers = new ArrayList<Header>();
    private int footerDividerY;
    private int footerTextY;

    public ClickGuiScreen(ModuleManager manager, ConfigManager configManager) {
        this.moduleManager = manager;
        this.configManager = configManager;
    }

    @Override
    public void initGui() {
        // Split modules into their display categories.
        List<Module> hud = new ArrayList<Module>();
        List<Module> behavior = new ArrayList<Module>();
        for (Module mod : moduleManager.getModules()) {
            if (mod.getCategory() == Module.Category.HUD) {
                hud.add(mod);
            } else {
                behavior.add(mod);
            }
        }
        this.hudModules = hud;
        this.behaviorModules = behavior;

        panelX = (this.width - PANEL_WIDTH) / 2;
        // Dry-run layout (baseY = 0) to measure total height.
        panelHeight = runLayout(0, false);
        panelY = (this.height - panelHeight) / 2;
        // Real layout pass with the centered origin.
        runLayout(panelY, true);

        // Button to open the HUD position editor.
        this.buttonList.add(new GuiButton(0, this.width / 2 - 80, panelY + panelHeight + 10, 160, 20, "Edit HUD Positions"));
    }

    /**
     * Computes the vertical layout of the panel. When {@code populate} is true,
     * the rows, headers and footer coordinates are written to this screen's
     * fields. Returns the total panel height measured from {@code baseY}.
     *
     * <p>Running the same code with {@code populate = false} (baseY = 0)
     * guarantees the measured height always matches the populated layout.</p>
     */
    private int runLayout(int baseY, boolean populate) {
        if (populate) {
            rows.clear();
            headers.clear();
        }

        int y = baseY + TITLE_BLOCK; // title block (text + spacing)
        y += ACCENT;                 // frost accent line sits at TITLE_BLOCK
        y += SECTION_GAP;
        if (populate) {
            headers.add(new Header("HUD", y));
        }
        y += HEADER_BLOCK;
        for (Module mod : hudModules) {
            if (populate) {
                rows.add(new Row(mod, panelX + PADDING, y, PANEL_WIDTH - PADDING * 2, ROW_HEIGHT));
            }
            y += ROW_HEIGHT;
        }

        y += SECTION_GAP;
        if (populate) {
            headers.add(new Header("MODULES", y));
        }
        y += HEADER_BLOCK;
        for (Module mod : behaviorModules) {
            if (populate) {
                rows.add(new Row(mod, panelX + PADDING, y, PANEL_WIDTH - PADDING * 2, ROW_HEIGHT));
            }
            y += ROW_HEIGHT;
        }

        y += FOOTER_GAP;
        if (populate) {
            this.footerDividerY = y;
        }
        y += FOOTER_TEXT_GAP;
        if (populate) {
            this.footerTextY = y;
        }
        y += FONT_HEIGHT;
        return y + BOTTOM_PAD - baseY;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        // Dim the world behind the GUI.
        GuiScreen.drawRect(0, 0, this.width, this.height, YukiTheme.DARK_OVERLAY);

        // Panel card.
        GuiScreen.drawRect(panelX, panelY, panelX + PANEL_WIDTH, panelY + panelHeight, YukiTheme.PANEL_BG);

        // Title: "Yuki" in snow white, "ClickGUI" in frost blue, centered as one block.
        String titleMain = "Yuki";
        String titleSub = " ClickGUI";
        int mainW = this.fontRendererObj.getStringWidth(titleMain);
        int subW = this.fontRendererObj.getStringWidth(titleSub);
        int titleX = panelX + (PANEL_WIDTH - mainW - subW) / 2;
        int titleY = panelY + 10;
        this.fontRendererObj.drawString(titleMain, titleX, titleY, YukiTheme.SNOW_WHITE, false);
        this.fontRendererObj.drawString(titleSub, titleX + mainW, titleY, YukiTheme.FROST_BLUE, false);

        // Frost accent line beneath the title (the Yuki signature).
        int accentY = panelY + TITLE_BLOCK;
        GuiScreen.drawRect(panelX, accentY, panelX + PANEL_WIDTH, accentY + ACCENT, YukiTheme.FROST_BLUE);

        // Resolve the hovered row once per frame (used for both rows and footer).
        Module hovered = null;
        for (Row row : rows) {
            if (mouseX >= row.x && mouseX <= row.x + row.w
                    && mouseY >= row.y && mouseY <= row.y + row.h) {
                hovered = row.module;
                break;
            }
        }

        // Category headers.
        for (Header header : headers) {
            this.fontRendererObj.drawString(header.label, panelX + PADDING, header.textY, YukiTheme.FROST_BLUE, false);
        }

        // Module rows.
        for (Row row : rows) {
            boolean enabled = row.module.isEnabled();
            boolean isHovered = (row.module == hovered);

            // Background tints replace hard borders.
            if (enabled) {
                GuiScreen.drawRect(row.x, row.y, row.x + row.w, row.y + row.h, YukiTheme.ENABLED_TINT);
            }
            if (isHovered) {
                GuiScreen.drawRect(row.x, row.y, row.x + row.w, row.y + row.h, YukiTheme.HOVER_TINT);
            }

            // Enabled accent bar on the left edge.
            if (enabled) {
                GuiScreen.drawRect(row.x, row.y, row.x + 2, row.y + row.h, YukiTheme.SAKURA_PINK);
            }

            // Module name.
            this.fontRendererObj.drawString(row.module.getName(), row.x + 10, row.y + 7, YukiTheme.SNOW_WHITE, false);

            // Softened ON / OFF indicator.
            String status = enabled ? "ON" : "OFF";
            int statusColor = enabled ? YukiTheme.SAKURA_PINK : YukiTheme.SLATE_GRAY;
            int statusWidth = this.fontRendererObj.getStringWidth(status);
            this.fontRendererObj.drawString(status, row.x + row.w - 8 - statusWidth, row.y + 7, statusColor, false);
        }

        // Footer divider + description line.
        GuiScreen.drawRect(panelX + PADDING, footerDividerY, panelX + PANEL_WIDTH - PADDING, footerDividerY + 1, YukiTheme.DIVIDER);
        String footer = (hovered != null)
                ? hovered.getDescription()
                : "Hover a module to view its description.";
        int footerColor = (hovered != null) ? YukiTheme.FROST_BLUE : YukiTheme.SLATE_GRAY;
        this.fontRendererObj.drawString(footer, panelX + PADDING, footerTextY, footerColor, false);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (mouseButton == 0) {
            for (Row row : rows) {
                if (mouseX >= row.x && mouseX <= row.x + row.w
                        && mouseY >= row.y && mouseY <= row.y + row.h) {
                    row.module.toggle();
                    break;
                }
            }
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 0) {
            Minecraft.getMinecraft().displayGuiScreen(new GuiEditScreen(moduleManager, configManager));
        }
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

    /* === Layout data structures === */

    private static class Row {
        final Module module;
        final int x;
        final int y;
        final int w;
        final int h;

        Row(Module module, int x, int y, int w, int h) {
            this.module = module;
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
        }
    }

    private static class Header {
        final String label;
        final int textY;

        Header(String label, int textY) {
            this.label = label;
            this.textY = textY;
        }
    }
}
