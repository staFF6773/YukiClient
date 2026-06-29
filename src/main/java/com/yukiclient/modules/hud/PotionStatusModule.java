package com.yukiclient.modules.hud;

import com.yukiclient.modules.Module;
import com.yukiclient.theme.YukiTheme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Potion Status Module.
 *
 * Lists the player's currently active potion effects with their level and
 * remaining time, stacked vertically inside a frosted panel.
 */
public class PotionStatusModule extends Module {
    private final Minecraft mc = Minecraft.getMinecraft();

    private static final int LINE_HEIGHT = 10;
    private final int paddingX = 6;
    private final int paddingY = 4;
    private final int lineGap = 1;

    public PotionStatusModule() {
        super("PotionStatus", "Lists your active potion effects.", Module.Category.HUD);
        // Off by default; mostly useful while buffed.
        this.enabled = false;
        this.x = 200;
        this.y = 10;
        this.width = 90;
        this.height = 20;
    }

    @Override
    public void render() {
        if (mc.thePlayer == null) {
            return;
        }

        List<String> lines = new ArrayList<String>();
        Collection<PotionEffect> effects = mc.thePlayer.getActivePotionEffects();
        if (effects != null) {
            for (PotionEffect effect : effects) {
                lines.add(formatEffect(effect));
            }
        }
        if (lines.isEmpty()) {
            lines.add("No active effects");
        }

        int maxTextWidth = 0;
        for (String line : lines) {
            maxTextWidth = Math.max(maxTextWidth, mc.fontRendererObj.getStringWidth(line));
        }

        int boxWidth = maxTextWidth + paddingX * 2;
        int boxHeight = lines.size() * LINE_HEIGHT + (lines.size() - 1) * lineGap + paddingY * 2;

        YukiTheme.drawFrostPanel(this.x, this.y, boxWidth, boxHeight);

        int textX = this.x + paddingX;
        int textY = this.y + paddingY;
        for (String line : lines) {
            mc.fontRendererObj.drawString(line, textX, textY, YukiTheme.SNOW_WHITE, false);
            textY += LINE_HEIGHT + lineGap;
        }

        // Update dimensions for the GUI editor.
        this.width = boxWidth;
        this.height = boxHeight;
    }

    /**
     * Formats a single effect as "Name [level] m:ss" (level omitted at I).
     */
    private String formatEffect(PotionEffect effect) {
        Potion potion = Potion.potionTypes[effect.getPotionID()];
        String name = potion != null ? I18n.format(potion.getName()) : ("Effect " + effect.getPotionID());

        int amplifier = effect.getAmplifier();
        if (amplifier > 0) {
            name = name + " " + (amplifier + 1);
        }

        int duration = effect.getDuration();
        String time;
        if (duration >= 1000000) {
            // Effectively infinite (e.g. beacon/ambient effects).
            time = "**:**";
        } else {
            int totalSeconds = duration / 20;
            time = String.format("%d:%02d", totalSeconds / 60, totalSeconds % 60);
        }
        return name + " " + time;
    }
}
