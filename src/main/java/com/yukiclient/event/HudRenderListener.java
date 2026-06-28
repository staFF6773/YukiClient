package com.yukiclient.event;

import com.yukiclient.gui.GuiEditScreen;
import com.yukiclient.modules.Module;
import com.yukiclient.modules.ModuleManager;
import com.yukiclient.modules.hud.YukiLogo;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;

/**
 * Core snow-themed render engine for YukiClient.
 * Hooks into Forge's post-render overlay event to draw all active HUD modules.
 */
public class HudRenderListener {
    private final ModuleManager moduleManager;

    public HudRenderListener(ModuleManager moduleManager) {
        this.moduleManager = moduleManager;
    }

    @SubscribeEvent
    public void onRenderGameOverlay(RenderGameOverlayEvent.Post event) {
        // Only render after everything else has been drawn
        if (event.type != RenderGameOverlayEvent.ElementType.ALL) {
            return;
        }

        // The HUD editor renders modules itself, so don't draw the in-game HUD
        // behind the editor's translucent overlay (avoids ghosting/double images).
        if (Minecraft.getMinecraft().currentScreen instanceof GuiEditScreen) {
            return;
        }

        // Render the YukiClient watermark logo in the corner.
        // It is separate from modules so it cannot be toggled or edited.
        YukiLogo.render(event);

        // Render all enabled snow-themed HUD modules.
        // Use the internal view and indexed iteration to avoid per-frame allocations.
        ArrayList<Module> modules = moduleManager.getModulesView();
        for (int i = 0, n = modules.size(); i < n; i++) {
            Module mod = modules.get(i);
            if (mod.isEnabled()) {
                mod.renderScaled();
            }
        }
    }
}